# SDD — Plataforma de Agendamento por Bot (Telegram)

**Data:** 2026-06-25
**Metodologia:** Híbrido TDD+SDD (DDD-lite na linguagem), camada dominante TDD
**Baseado no ADR:** v1.0 · **Glossário:** `CONTEXT.md`

---

## Estrutura de Arquivos Esperada

```
gerenciador-atendimentos/
├── docker-compose.yml
├── .env / .env.example
├── .github/workflows/ci.yml
├── backend/
│   ├── pom.xml
│   ├── Dockerfile / .dockerignore
│   └── src/
│       ├── main/java/com/plataforma/agendamentos/
│       │   ├── AgendamentosApplication.java
│       │   ├── domain/                      # Java puro, sem infra
│       │   │   ├── conta/      (Conta)
│       │   │   ├── servico/    (Servico)
│       │   │   ├── cliente/    (Cliente, OrigemCliente)
│       │   │   ├── horario/    (HorarioAtendimento, DiaSemana)
│       │   │   └── agendamento/(Agendamento, StatusAgendamento, OrigemAgendamento,
│       │   │                    Slot, CalculadoraDisponibilidade)
│       │   ├── application/
│       │   │   ├── port/in/    (use cases: interfaces)
│       │   │   ├── port/out/   (repositories, notificação: interfaces)
│       │   │   └── service/    (implementações dos use cases)
│       │   └── adapter/
│       │       ├── in/web/       (controllers REST, DTOs, segurança JWT)
│       │       ├── in/telegram/  (long polling, roteamento de comandos)
│       │       ├── out/persistence/ (JPA entities, repositórios, mappers)
│       │       └── out/telegram/    (envio de mensagens/calendário)
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/V1__baseline.sql   (Flyway)
│       └── test/java/...        (espelha a árvore; domínio testado sem Spring)
└── frontend/                    # Next.js (App Router) + TypeScript
    ├── package.json / Dockerfile / .dockerignore
    └── app/                     (login, serviços, calendário, link do bot, cadastro manual)
```

**Regra de enum (briefing #1):** cada enum em arquivo próprio. `StatusAgendamento`,
`OrigemCliente`, `OrigemAgendamento`, `DiaSemana` são arquivos `.java` separados — nunca
inner enum.

---

## Módulos

### domain/agendamento — núcleo de regras

**Responsabilidade:** representar o agendamento e calcular slots/disponibilidade. Onde
moram as regras invioláveis. Java puro, testável sem Spring.

**Entrada (para cálculo de disponibilidade):**
```
- Servico (duracao_min)
- List<HorarioAtendimento> da conta
- List<Agendamento> CONFIRMADOS da conta
- intervalo de datas (hoje .. hoje+30)
- "agora" (relógio injetado, para testabilidade)
```
**Saída:**
```
- List<Slot> livres (cada Slot = inicio, fim) por dia
- erros: nenhum (cálculo puro); coleção vazia se não há disponibilidade
```
**Regras que nunca podem ser violadas:**
- Slot gerado encadeado: `inicio = hora_inicio + n*duracao_min`; só vale se
  `inicio+duracao_min <= hora_fim`.
- Slot livre = futuro (> agora) E sem sobreposição com nenhum Agendamento CONFIRMADO.
- Dois CONFIRMADOS não se sobrepõem na mesma conta.
- `fim = inicio + servico.duracao_min`.

### application/service — casos de uso

**Responsabilidade:** orquestrar use cases chamando **ports** (interfaces). Sem regra de
negócio do núcleo (delega ao domínio) e sem detalhe de infra.

**Regras:**
- Injeta apenas ports via construtor. Nunca adapter concreto, nunca `*Impl`.
- Transação na operação de agendar/remarcar (garante a invariante de overlap).
- Toda operação recebe/filtra por `conta_id` (multi-tenant).

**Use cases (port/in) do MVP:**
| Use case | Ação |
|---|---|
| `CriarContaUseCase` | signup (nome, email, senha) → gera `bot_deep_link_token` |
| `AutenticarUseCase` | login → JWT |
| `GerenciarServicoUseCase` | CRUD de serviço (nome, duração, descrição, preço opcional, ativo) |
| `GerenciarHorarioUseCase` | definir janelas de atendimento por dia |
| `ConsultarDisponibilidadeUseCase` | dias/slots livres p/ um serviço (próx. 30 dias) |
| `ListarServicosUseCase` | serviços ativos de uma conta (bot) |
| `AgendarUseCase` | criar Agendamento CONFIRMADO (valida overlap/janela/futuro) |
| `CancelarAgendamentoUseCase` | status → CANCELADO (cliente via bot ou dono via web) |
| `RemarcarAgendamentoUseCase` | move o mesmo registro p/ novo horário (revalida) |
| `CadastrarClienteManualUseCase` | cliente MANUAL (telefone + serviço desejado) |
| `VerAgendaUseCase` | agregação por dia p/ o calendário web |
| `ResolverContaPorTokenUseCase` | mapear `start=<token>` → conta (deep link) |

### adapter/in/telegram — bot (long polling)

**Responsabilidade:** receber updates do Telegram, mapear comando → use case, devolver
resposta (texto / teclado de calendário inline). Sem regra de negócio.

### adapter/in/web — API REST + JWT

**Responsabilidade:** expor endpoints, autenticar via JWT, converter DTO↔domínio. DTOs
`*Request`/`*Response` vivem aqui; classes de domínio não saem na resposta.

### adapter/out/persistence — JPA

**Responsabilidade:** implementar os ports de repositório; converter Entity↔Domain
(mapper no adapter, nunca no domínio).

---

## Contrato com Sistema Externo — Telegram Bot API

**Modo:** long polling (`getUpdates`). O backend faz chamadas **de saída**; nenhum container
recebe conexão de fora. Token via `.env` (`TELEGRAM_BOT_TOKEN`).

**O que recebemos (updates):**
| Evento | Conteúdo usado |
|---|---|
| `/start <token>` | `bot_deep_link_token` → resolve a conta; `from.id` = `telegram_user_id`, `from.first_name`/`username` = nome |
| Mensagem de texto / comando | intenção (listar serviços, ver dias, agendar, cancelar, remarcar) |
| `callback_query` | seleção no teclado inline (dia/horário/serviço escolhido) |

**O que enviamos:**
| Ação | Método Telegram |
|---|---|
| Texto (lista de serviços, confirmação "tudo certo!") | `sendMessage` |
| Calendário / horários (teclado inline) | `sendMessage` com `inline_keyboard` |
| Atualizar seleção | `editMessageText` / `answerCallbackQuery` |

**Em caso de falha (Telegram fora / timeout):**
- O agendamento só é considerado confirmado após **persistir no banco**; o envio da
  mensagem de confirmação é best-effort (retry simples + log). Persistência é a fonte da verdade.
- Erros de envio são logados; não derrubam a transação de negócio.
- Identidade: o bot **não pede telefone**; usa `telegram_user_id`.

---

## Contrato da API REST (área web)

Base: `/api`. Auth: `Authorization: Bearer <jwt>` (exceto signup/login). Todo recurso é
escopado pela conta do JWT (`conta_id` nunca vem do cliente).

| Método | Rota | Entrada | Saída |
|---|---|---|---|
| POST | `/auth/signup` | `{nome,email,senha}` | `{contaId, botDeepLink}` |
| POST | `/auth/login` | `{email,senha}` | `{token}` |
| GET/POST/PUT/DELETE | `/servicos` | `ServicoRequest` | `ServicoResponse` |
| GET/PUT | `/horarios` | `HorarioRequest[]` | `HorarioResponse[]` |
| GET | `/agenda?mes=YYYY-MM` | — | `{dia, quantidade}[]` (calendário web) |
| GET | `/agendamentos?data=YYYY-MM-DD` | — | `AgendamentoResponse[]` |
| POST | `/agendamentos/{id}/cancelar` | — | `AgendamentoResponse` |
| POST | `/clientes/manual` | `{nome,telefone,servicoId}` | `ClienteResponse` |
| GET | `/conta/bot-link` | — | `{botDeepLink}` |

> Erros: padrão `{erro, mensagem}` + HTTP status. 401 sem/JWT inválido; 403 recurso de
> outra conta; 409 conflito de horário (overlap); 422 validação.

---

## Schema (PostgreSQL, Flyway V1)

Tabelas: `conta`, `horario_atendimento`, `servico`, `cliente`, `agendamento`. Toda tabela
de negócio carrega `conta_id` (FK). Campos conforme `CONTEXT.md`.

Constraints-chave:
- `conta.email` único.
- `cliente`: CHECK (`telegram_user_id` IS NOT NULL OR `telefone` IS NOT NULL).
- `agendamento.status`/`origem`, `cliente.origem`: validados por enum na app (string no banco).
- Overlap: garantido na aplicação dentro de transação (MVP); índice por `(conta_id, inicio)`
  para a consulta. (Exclusion constraint via `btree_gist` fica como reforço pós-MVP.)

---

## Fora do Escopo Técnico

- WhatsApp / Evolution API, webhook do Telegram, multi-bot (Modelo B).
- Cobrança/faturamento, relatórios financeiros.
- Lembretes automáticos, múltiplos profissionais por conta.
- Refresh token / OAuth social (login é email+senha → JWT simples no MVP).
- Exclusion constraint de overlap no banco (reforço pós-MVP; MVP valida na app).

---

## Convenções obrigatórias

- **Nomenclatura:** domínio e tabelas seguem o `CONTEXT.md`. DTOs de entrada `*Request`/
  `*Command`; de saída `*Response`/`*View`.
- **Inversão de dependência:** application/domínio enxergam só ports (interfaces); injeção
  por construtor; nunca `@Autowired` em campo nem adapter concreto.
- **Enum:** sempre em arquivo próprio.
- **Conversão domínio↔DTO / domínio↔Entity:** no adapter, nunca no domínio.
- **Domínio puro:** sem imports de Spring/JPA/Telegram, sem anotações de infra.
- **Tratamento de erro:** exceções de domínio próprias (ex.: `HorarioIndisponivelException`,
  `ConflitoDeAgendamentoException`) → mapeadas para HTTP no adapter web.
- **Logging:** SLF4J; logar falhas de integração com Telegram sem derrubar negócio.
- **Testes:** domínio testado sem Spring (`@SpringBootTest` só em testes de adapter/integração);
  TDD para as regras invioláveis (overlap, slot, futuro, janela).
- **Tempo:** relógio injetável (`Clock`) no domínio para testar "horário futuro".
