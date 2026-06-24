# SDD — gerenciador-atendimentos

**Data:** 2026-06-24
**Metodologia:** Híbrido (TDD dominante + DDD-lite + SDD) — ver ADR.md
**Baseado no ADR:** v1.0
**Glossário canônico:** CONTEXT.md

---

## Estrutura de Arquivos Esperada

```
gerenciador-atendimentos/
├── docker-compose.yml
├── .env.example                 # tokens/segredos (NUNCA commitar .env real)
├── .github/workflows/ci.yml     # testes antes do build das imagens
│
├── backend/
│   ├── pom.xml
│   ├── Dockerfile               # multi-stage
│   ├── .dockerignore
│   └── src/
│       ├── main/java/com/plataforma/agendamentos/
│       │   ├── domain/                      # Java puro, sem Spring/JPA/Telegram
│       │   │   ├── conta/                   # Conta
│       │   │   ├── servico/                 # Servico
│       │   │   ├── cliente/                 # Cliente, OrigemCliente (enum)
│       │   │   ├── horario/                 # HorarioAtendimento, DiaSemana (enum)
│       │   │   └── agendamento/             # Agendamento, StatusAgendamento, OrigemAgendamento, Slot
│       │   ├── application/
│       │   │   ├── port/in/                 # use cases (interfaces)
│       │   │   ├── port/out/                # repositórios e notificação (interfaces)
│       │   │   └── service/                 # implementações dos use cases
│       │   └── adapter/
│       │       ├── in/web/                  # controllers REST, DTOs *Request/*Response, segurança JWT
│       │       ├── in/telegram/             # bot (long polling), comandos, teclados/calendário
│       │       ├── out/persistence/         # JPA entities + repos + mappers Entity↔Domain
│       │       └── out/telegram/            # envio de mensagens (impl. do port de notificação)
│       ├── main/resources/
│       │   ├── application.yml
│       │   └── db/migration/                # Flyway V1__*.sql ...
│       └── test/java/...                    # testes de domínio sem Spring; testes de adapter com Spring
│
└── frontend/                                # Next.js App Router + TS + Tailwind
    ├── package.json
    ├── Dockerfile
    ├── .dockerignore
    └── app/                                 # rotas (login, dashboard, serviços, calendário)
```

---

## Módulos (use cases do núcleo — contratos)

> Todos os use cases recebem/derivam a `conta` do contexto (JWT na web; deep link token no bot)
> e **toda consulta/escrita filtra por `conta_id`** (regra 4 do CONTEXT.md).

### CriarConta + Login (web)
- **Entrada:** `CriarContaRequest{nome, email, senha}` / `LoginRequest{email, senha}`.
- **Saída:** `ContaResponse{id, nome, email, botDeepLinkToken}` / `TokenResponse{jwt}`.
- **Regras:** email único; senha em **BCrypt**; ao criar conta, gerar `bot_deep_link_token` único.
- **Erros:** email já existe; credenciais inválidas.

### CriarServico / ListarServicos (web + bot)
- **Entrada:** `CriarServicoRequest{nome, duracaoMin, descricao, preco?}`.
- **Saída:** `ServicoResponse{id, nome, duracaoMin, descricao, preco?, ativo}`.
- **Regras:** `duracaoMin > 0`; `preco` opcional/nullable; só serviços `ativo=true` aparecem ao cliente no bot.
- **Erros:** duração inválida; serviço não pertence à conta.

### DefinirHorarioAtendimento (web)
- **Entrada:** `HorarioAtendimentoRequest{diaSemana, horaInicio, horaFim}` (1..N por conta).
- **Regras:** `horaInicio < horaFim`; janelas definem a base da disponibilidade.

### ConsultarDisponibilidade (bot)
- **Entrada:** `servicoId`, faixa de datas (ou "próximos dias").
- **Saída:** dias/horários de início livres (grade de `granularidade_min`, default 30).
- **Regras:** disponibilidade = grade dentro de `HorarioAtendimento` − slots ocupados por
  agendamentos **CONFIRMADO**, considerando que o serviço ocupa `ceil(duracao/granularidade)`
  fatias consecutivas; só horários **futuros** (fuso `America/Sao_Paulo`).

### AgendarConsulta (bot)
- **Entrada:** `AgendarCommand{contaToken, servicoId, inicio, clienteTelegram{userId, nome}}`.
- **Saída:** `AgendamentoResponse{id, inicio, fim, status=CONFIRMADO}`.
- **Regras invioláveis:** futuro; dentro da janela; **sem sobreposição** com outro CONFIRMADO
  (intervalo `[inicio, fim)`); `fim = inicio + servico.duracaoMin`; cria Cliente origem `BOT`
  se ainda não existe (por `telegram_user_id` + conta). Status nasce `CONFIRMADO`.
- **Erros:** slot ocupado; fora da janela; horário no passado; serviço inativo/inexistente.

### CancelarAgendamento / RemarcarAgendamento (bot e web)
- **Cancelar:** muda status para `CANCELADO` (libera o horário).
- **Remarcar:** **atualiza o mesmo registro** (mantém `id`, muda `inicio`/`fim`), revalidando
  as regras de AgendarConsulta. Atualiza `atualizado_em`.
- **Recusar (dono, web):** equivale a cancelar (status `CANCELADO`).

### CadastrarClienteManual (web)
- **Entrada:** `ClienteManualRequest{nome, telefone, servicoDesejadoId?}`.
- **Regras:** origem `MANUAL`; tem `telefone` (sem `telegram_user_id`).

### CalendárioDeAgendamentos (web)
- **Saída:** agregação por dia (quantidade de agendamentos) + lista do dia. Filtrada por conta.

---

## Contratos entre Módulos

| De | Para | O que passa | Formato |
|---|---|---|---|
| adapter/in/web | application (port/in) | comandos/requests do dono | `*Request` / `*Command` (DTO) |
| adapter/in/telegram | application (port/in) | comandos do cliente (deep link, escolha de serviço/horário) | `*Command` (DTO) |
| application | adapter/out/persistence | objetos de **domínio** | entidades de domínio (via port/out) |
| application | adapter/out/telegram | mensagem a enviar | DTO de notificação (via port/out) |
| application | adapter/in/* | resultado do use case | objeto de domínio → adapter converte em `*Response` |

---

## Contrato com Sistema Externo — Telegram Bot API

- **Modo:** long polling (`getUpdates`). Backend faz chamadas **de saída**; sem URL pública.
- **Identidade da conta:** deep link `t.me/SeuBot?start=<bot_deep_link_token>` → o `start`
  resolve a `conta` no primeiro contato; vínculo `telegram_user_id ↔ conta` é mantido na sessão de conversa.
- **Envia:** mensagens de texto, teclados inline (lista de serviços, calendário de dias/horários), confirmações.
- **Recebe:** `message` (texto/comandos `/start`), `callback_query` (cliques nos teclados).
- **Comportamento em falha:**
  - Telegram indisponível → retry com backoff no envio; nenhuma exceção vaza para o domínio.
  - Update malformado/serviço inexistente → responder mensagem de erro amigável, não derrubar o polling.
  - **Idempotência:** um mesmo `callback_query` reprocessado não pode gerar agendamento duplicado.
- **Token:** vem de `.env` (`TELEGRAM_BOT_TOKEN`), nunca commitado.

---

## Fora do Escopo Técnico

- WhatsApp/Evolution API; webhook do Telegram (fica polling no MVP).
- Cobrança/pagamento; dashboard de faturamento.
- Múltiplos profissionais/agendas por conta; múltiplos usuários (logins) por conta.
- Lembrete automático; UI de configuração de `granularidade_min` (usa default 30 no MVP).
- Internacionalização/fuso por conta (fixo `America/Sao_Paulo`).

---

## Convenções obrigatórias

- **Nomenclatura:** ports de entrada = `*UseCase`; ports de saída = `*Repository`/`*Port`;
  DTOs = `*Request`/`*Response`/`*Command`. Enums em **arquivo próprio** (sem inner enum).
- **Inversão de dependência:** injetar **interface** (port), nunca implementação; injeção por
  **construtor**; domínio/application não importam Spring/JPA/Telegram.
- **Conversão:** Entity JPA ↔ Domain no adapter de persistência; Domain ↔ DTO no adapter web.
- **Tratamento de erro:** regras de domínio violadas → exceção de domínio específica;
  adapters traduzem para HTTP (web) ou mensagem amigável (bot).
- **Tempo:** persistir em UTC; converter para `America/Sao_Paulo` na borda.
- **Multi-tenant:** toda query e escrita filtram por `conta_id`.
- **Testes:** domínio com JUnit puro (sem `@SpringBootTest`); regra inviolável tem teste antes da implementação (TDD).
- **Logging:** sem dados sensíveis (token, senha) em log.
