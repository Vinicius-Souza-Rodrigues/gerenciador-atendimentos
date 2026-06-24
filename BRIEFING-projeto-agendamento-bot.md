# Briefing — Plataforma de Agendamento por Bot (Telegram → multiplataforma)

> Documento para colar no `/project-bootstrap` de um projeto NOVO (pasta vazia).
> Serve para responder a calibração (9 perguntas) e o PRD (6 perguntas) sem mal-entendido.

---

## ⚠️ Correção em relação à tentativa anterior (leia primeiro)

A tentativa anterior travou por mal-entendido de escopo. O novo projeto **muda 3 pontos**:

1. **Telegram primeiro**, não WhatsApp. O WhatsApp (via Evolution API) fica como evolução futura; o Telegram tem integração mais direta para começar.
2. **É um SaaS multi-conta**, não um sistema de um único negócio. Qualquer pessoa ou estabelecimento cria uma conta e recebe um bot.
3. **Bot com calendário interativo + conversa**, e cada conta tem um **link do bot** para compartilhar com clientes.

---

## Resumo em uma frase

Uma plataforma onde qualquer pessoa ou estabelecimento cria uma conta, recebe um **bot de Telegram** para que seus clientes agendem horários por conversa/calendário, e acompanha tudo por uma **área web** própria com calendário de agendamentos.

---

## Problema que resolve

Pessoas e estabelecimentos que marcam horário (consultas, serviços, atendimentos) gerenciam isso na mão — DM, telefone, papel. Dá esquecimento, conflito de horário e nenhuma visão da agenda. A plataforma automatiza o agendamento via bot e centraliza a visão na web.

## Quem usa (2 atores)

| Ator | Quem é | Como interage |
|---|---|---|
| **Dono da conta** | Pessoa comum ou estabelecimento | Cria conta, usa a **área web** (serviços, calendário, link do bot, cadastro manual) |
| **Cliente final** | Quem quer marcar horário | Conversa com o **bot de Telegram** pelo link que o dono compartilhou |

---

## Funcionalidades do MVP (o núcleo)

1. **Criar conta + login** (pessoa ou estabelecimento) — signup self-service; cada dono só vê a própria conta.
2. **Provisionar o bot** — a conta recebe um link de bot do Telegram (1 bot da plataforma + **deep link por conta**) para compartilhar.
3. **Área web — gerenciar serviços**: o dono cria serviços com **nome, duração, descrição e preço (opcional)**. Todo agendamento é feito sobre um serviço já criado.
4. **Bot — listar serviços**: o cliente pede e o bot mostra os serviços disponíveis daquela conta.
5. **Bot — consultar disponibilidade**: cliente pede "quais dias estão disponíveis?" e o bot responde com um **calendário** no Telegram.
6. **Bot — agendar por conversa**: o cliente escolhe um **serviço** + horário → o bot valida e **confirma na hora** ("tudo certo!"); o **dono pode recusar/cancelar depois** pela web.
7. **Bot — cancelar/remarcar**: o cliente cancela ou remarca conversando com o próprio bot.
8. **Área web — calendário de agendamentos**: o dono vê os dias com mais marcações e a quantidade de agendamentos por dia.
9. **Área web — link do bot**: o dono copia o link para mandar aos clientes (ou usar ele mesmo).
10. **Área web — cadastro manual de cliente**: o dono cadastra na mão um cliente (telefone + serviço desejado).

> O MVP entrega valor quando o ciclo **conta → bot → agendamento → visão na web** funciona ponta a ponta.

## Funcionalidades pós-MVP (fora do escopo agora)

- **WhatsApp** (e outras plataformas) além do Telegram.
- **Dashboard de faturamento** (consultas pagas) — domínio financeiro BR.
- **Lembrete automático** 1h antes da consulta.
- **Vários profissionais por conta** (agendas separadas dentro de um estabelecimento).
- **Espaço de opções extras** configuráveis por conta.

---

## Arquitetura desejada

- **Backend Hexagonal (Ports & Adapters)** — núcleo de domínio isolado; bot, web e banco são adapters.
- **TDD (Test Driven Development)** — sim, casa perfeitamente com Hexagonal: o domínio é testável sem tocar Telegram/banco/HTTP. O bot e o site são só adapters mockáveis.
- **Docker com redes isoladas** (detalhado na seção abaixo).
- **`.dockerignore`**, containers separados.
- **CI/CD com workflows** (testes rodando no pipeline, build das imagens).

### Docker — topologia detalhada

**Containers (todos separados):**

| Container | O que roda | Exposto ao host? |
|---|---|---|
| `frontend` | Next.js (área web) | Sim (ex.: porta 3000) |
| `backend` | Spring Boot (API REST + adapter do bot Telegram) | Só o necessário (ver polling vs webhook) |
| `db` | PostgreSQL | **Não** em produção; em dev, opcional |
| `proxy` *(opcional)* | Nginx/Traefik — só se usar **webhook** do Telegram | Sim (443) |

**Redes (o isolamento que você pediu):**

```
   ┌────────────┐
   │  frontend  │
   └─────┬──────┘
         │  rede: frontend-net
   ┌─────┴──────┐
   │   backend  │
   └─────┬──────┘
         │  rede: backend-net   (SEPARADA)
   ┌─────┴──────┐
   │     db     │
   └────────────┘
```

- `frontend-net`: só `frontend` + `backend`.
- `backend-net`: só `backend` + `db`.
- O `frontend` **não está** na `backend-net`, então **não enxerga o banco**. O banco só é alcançável pelo backend. É esse o isolamento.

**Exposição de portas:**
- `db` **não** publica porta no host em produção (sem `ports:`) — fala só pela rede interna.
- `frontend` publica a porta web.
- `backend` publica porta só se o front precisar chamá-lo direto; senão fica interno.

**Telegram dentro do Docker — 2 modos (decisão de infra):**
- **Long polling** (recomendado pra começar): o backend faz chamadas **de saída** para `api.telegram.org`. Precisa só de **internet de saída** — nenhum container precisa receber conexão de fora. Funciona em dev sem URL pública.
- **Webhook**: o Telegram chama um endpoint **HTTPS público** seu. Exige o `proxy` exposto na 443 encaminhando pro backend, com domínio + HTTPS. Mais eficiente em produção.

**Boas práticas que já entram no setup:**
- **`.dockerignore`** por serviço (ignora `node_modules`, `target/`, `.git`, `.env`, artefatos de build).
- **Multi-stage build** (compila numa imagem, roda numa enxuta).
- **Healthcheck** no `db` + `depends_on: condition: service_healthy` no backend.
- **Volume** nomeado pra persistir o Postgres.
- **`.env`** pros segredos (token do bot, senha do banco) — nunca commitado.
- Backend **roda os testes no CI** antes do build da imagem (TDD no pipeline).

### Padrões de código obrigatórios

Regras que valem para **todo o projeto**, sem exceção. O agente deve aplicá-las sem precisar ser lembrado e deve **parar e perguntar** se houver dúvida sobre como encaixá-las em algum caso concreto.

#### 1. Enum sempre em arquivo próprio

Nenhum `enum` pode ser declarado como inner class ou inner type dentro de outra classe. Cada enum tem seu próprio arquivo `.java` (ou `.ts`, dependendo da camada).

```
✅ domain/agendamento/StatusAgendamento.java
✅ domain/cliente/OrigemCliente.java

❌ Agendamento.java  →  static enum Status { ... }   // proibido
```

#### 2. DTOs para transporte entre camadas

Classes de domínio **não trafegam** entre camadas. Sempre usar DTOs:

- **Entrada** (web → application): `*Request` ou `*Command` — recebe dados do adapter HTTP.
- **Saída** (application → web): `*Response` ou `*View` — dado que sai pelo adapter HTTP.
- **Interno** (entre serviços de aplicação): usar o objeto de domínio diretamente; não criar DTO intermediário desnecessário.

Conversão domínio ↔ DTO é responsabilidade do **adapter** ou de um método estático/factory no próprio DTO — nunca dentro da classe de domínio.

#### 3. Inversão de dependência — injetar interface, nunca implementação

O núcleo de domínio e a camada de aplicação **enxergam apenas ports (interfaces)**. Nunca chamar uma classe concreta (adapter, repositório JPA, client Telegram) diretamente.

```
✅ private final AgendamentoRepository repo;          // port (interface)
✅ private final NotificacaoPort notificacao;         // port (interface)

❌ private final AgendamentoRepositoryImpl repo;      // implementação concreta — proibido
❌ private final TelegramBotAdapter telegram;         // adapter concreto — proibido
```

A injeção é feita via **construtor** (não `@Autowired` em campo). O Spring resolve a implementação em tempo de execução — o código de aplicação não sabe qual implementação está rodando, e esse é o ponto.

#### 4. Outras convenções de arquitetura (Hexagonal)

| Regra | Detalhamento |
|---|---|
| **Ports de entrada** (`port/in`) | Interfaces que o adapter chama para acionar o domínio (ex.: `AgendarConsultaUseCase`). |
| **Ports de saída** (`port/out`) | Interfaces que o domínio chama para falar com o exterior (ex.: `AgendamentoRepository`, `EnviarMensagemPort`). |
| **Adapters** | Implementam ports de saída OU acionam ports de entrada — nunca lógica de negócio. |
| **Domínio** | Sem imports do Spring, sem anotações JPA, sem nada de infra. Puro Java. |
| **Conversão** | `Entity JPA → Domain` e `Domain → Entity JPA` — no adapter de persistência, não no domínio. |

---

### Decisão importante sobre o bot (resolva isso cedo)

"Cada conta tem um bot" pode ser feito de 2 jeitos no Telegram:

- **Modelo A — 1 bot da plataforma + deep link por conta** (recomendado p/ MVP):
  link `t.me/SeuBotDaPlataforma?start=conta123`. Um único token, simples, e o "link do bot pra compartilhar" sai de graça.
- **Modelo B — cada conta cria o próprio bot no BotFather** e cola o token:
  mais "dono do próprio bot", mas com atrito (cada usuário cria bot manualmente).

**DECIDIDO: Modelo A** (1 bot da plataforma + deep link por conta) no MVP. Modelo B fica como evolução.

### Comportamento do bot (decidido)

| Tema | Decisão |
|---|---|
| **Modelo do bot** | 1 bot da plataforma + **deep link por conta** (`t.me/SeuBot?start=conta123`). |
| **Listar serviços** | O cliente pode **pedir a lista de serviços** da conta pelo bot e escolher um ao agendar. |
| **Confirmação** | Automática na hora — o bot valida o horário e confirma ("tudo certo!"). O dono **pode recusar/cancelar depois** pela área web. |
| **Identidade do cliente** | Quem agenda **pelo bot** é identificado pela conta do **Telegram** (nome/usuário) — o bot **não pede telefone**. Telefone aparece só no **cadastro manual da web** (item 10). |
| **Profissionais por conta** | **Uma agenda por conta** no MVP. Vários profissionais por conta ficam pós-MVP. |
| **Cancelar / remarcar** | O cliente **pode cancelar e remarcar pelo próprio bot**. |

---

## Modelo de dados (ponto de partida para o SDD)

> Esboço para o outro chat não reinterpretar. O SDD refina os detalhes; aqui fica a intenção.

**Relações:**

```
Conta  1───N  Serviço
Conta  1───N  Cliente
Conta  1───N  Agendamento
Conta  1───N  HorarioAtendimento   (dias/horas em que a conta atende)
Cliente 1──N  Agendamento
Serviço 1──N  Agendamento
```

Toda tabela tem **`conta_id`** — é esse o isolamento multi-tenant por coluna.

**Conta** (o dono — pessoa ou estabelecimento; é o *tenant*)

| Campo | Observação |
|---|---|
| id | PK |
| nome | nome do dono/negócio |
| email | login (único) |
| senha_hash | autenticação |
| bot_deep_link_token | o `start=<token>` do link (`t.me/SeuBot?start=<token>`) |
| criado_em | |

**HorarioAtendimento** (disponibilidade da conta)

| Campo | Observação |
|---|---|
| id | PK |
| conta_id | FK |
| dia_semana | ex.: SEG..DOM |
| hora_inicio / hora_fim | janela de atendimento naquele dia |

**Serviço** (criado pelo dono na área web)

| Campo | Observação |
|---|---|
| id | PK |
| conta_id | FK |
| nome | |
| duracao_min | duração em minutos (define o tamanho do slot) |
| descricao | |
| preco | **opcional** (nullable) |
| ativo | se aparece na listagem do bot |
| criado_em | |

**Cliente** (pode vir de 2 origens)

| Campo | Observação |
|---|---|
| id | PK |
| conta_id | FK |
| nome | |
| telegram_user_id | **nullable** — preenchido se veio do **bot** |
| telefone | **nullable** — preenchido se veio do **cadastro manual** |
| origem | `BOT` ou `MANUAL` |
| criado_em | |

> Regra: todo Cliente tem **telegram_user_id OU telefone** (pelo menos um).

**Agendamento**

| Campo | Observação |
|---|---|
| id | PK |
| conta_id | FK |
| cliente_id | FK |
| servico_id | FK |
| inicio | data/hora de início |
| fim | = início + duração do serviço |
| preco_cobrado | snapshot do preço no momento da marcação (opcional) |
| status | `CONFIRMADO` / `CANCELADO` / `CONCLUIDO` |
| origem | `BOT` ou `MANUAL` |
| criado_em / atualizado_em | |

**Regras invioláveis do domínio (núcleo Hexagonal):**

1. Dois agendamentos **CONFIRMADOS não podem se sobrepor** na mesma conta (uma agenda só).
2. Agendamento só em **horário futuro** e **dentro da disponibilidade** (HorarioAtendimento) da conta.
3. A **duração** do agendamento vem do **serviço** escolhido.
4. Cliente, Serviço e Agendamento sempre pertencem à **mesma `conta_id`**.
5. Confirmação é automática → status nasce `CONFIRMADO`; recusa do dono ou cancelamento do cliente → `CANCELADO`.

---

## Sistemas externos

- **Telegram Bot API** (MVP) — dependência externa principal.
- **WhatsApp Business / Evolution API** (futuro).
- Documentar o **contrato com o Telegram** no SDD (o que envia, o que recebe, o que fazer em falha).

## Domínio financeiro

- O **serviço tem preço (opcional)** já no MVP — é só um campo cadastrado, **sem cobrança**.
- O **dashboard de faturamento** (somatório, relatórios de serviços pagos) é **pós-MVP**.
- Quando o financeiro entrar pra valer: é **BR** (R$ 1.234,56, formatação brasileira) — sinalizar na Validation Checklist.

---

## Stack escolhida (default — pode trocar na pergunta 8)

Baseado no seu histórico (havia JUnit no projeto anterior), a stack natural e compatível com Hexagonal + TDD:

- **Backend:** Java + Spring Boot + JUnit (Hexagonal + TDD muito maduro nesse ecossistema).
- **Frontend/área web:** Next.js (ou outro de sua preferência).
- **Banco:** PostgreSQL.
- **Bot:** biblioteca de Telegram do ecossistema escolhido.

> Não é obrigatório — é o caminho de menor atrito dado o que você já usa. Se preferir outra stack, troque aqui.

---

## Padrão de trabalho e pausas (anti-alucinação)

Preferência: **tarefas sólidas, com começo, meio e fim bem definidos** — pra não dar espaço pro agente desviar/alucinar.

- **Tarefas pequenas e fechadas**, cada uma com **critério de conclusão observável** (ex.: *"consigo criar um serviço e ele aparece na lista"*) — nunca *"implementar módulo X"*.
- **[HITL] por padrão**: o agente para ao fim de cada tarefa entregável e eu valido pelo **comportamento** antes de avançar. **[AFK] só** para tarefas triviais sem risco (ex.: criar estrutura de pastas).
- **Uma tarefa por vez** — não encadear várias fases sem validação no meio.
- **Validation Checklist obrigatória** ao fim de cada entrega; **qualquer incerteza de lógica → o agente PARA e pergunta**, não chuta.
- **Retomada detalhada após pausa**: ao voltar, o agente relê DECISIONS/ROADMAP e me diz o que foi feito, o que foi validado e a próxima tarefa — e só executa após eu confirmar.
- **Fases curtas no ROADMAP** (5–7 no total), cada uma com critério observável.

---

## RESPOSTAS PRONTAS — Calibração (9 perguntas)

1. **Solo ou equipe?** → Solo.
2. **Caminho base de projetos?** → (preencha com a pasta onde ficam seus projetos).
3. **Quais projetos ativos?** → este (plataforma de agendamento por bot).
4. **Padrão de pausa?** → **Retomada detalhada**: ao voltar, o agente resume última fase concluída, última entrega validada e próxima tarefa, e só executa após eu confirmar. Tarefas pequenas, [HITL] por padrão (ver seção "Padrão de trabalho e pausas").
5. **Domínio financeiro?** → Parcial: **serviço tem preço opcional** no MVP (só cadastro, sem cobrança); **dashboard de faturamento** é pós-MVP (BR).
6. **Sistemas externos?** → Sim: **Telegram Bot API** (MVP) e WhatsApp (futuro).
7. **Validação detalhada ou rápida?** → Detalhada (quero algo bem testado).
8. **Stack principal?** → Java + Spring Boot + JUnit / Next.js / PostgreSQL (ver seção acima).
9. **Tipo de produto?** → Misto: **API/backend** + **app com interface** + bot. Forte sinal de **TDD + SDD**, com alertas de contrato de API.

## RESPOSTAS PRONTAS — PRD (6 perguntas)

1. **Qual problema resolve?** → Pessoas/estabelecimentos gerenciam agendamentos na mão (DM, telefone, papel); dá esquecimento, conflito e nenhuma visão da agenda.
2. **Quem vai usar?** → Donos de conta (pessoa ou estabelecimento) na área web; clientes finais no bot de Telegram.
3. **Menor uso que já entrega valor?** → Criar conta com login → cadastrar um serviço → receber o link do bot → cliente vê os serviços e os dias disponíveis no Telegram, escolhe um serviço e agenda → dono vê esse agendamento no calendário web.
4. **Fora do escopo agora?** → WhatsApp/outras plataformas, dashboard de faturamento, lembrete 1h antes, vários profissionais por conta, opções extras configuráveis.
5. **Restrição técnica já conhecida?** → Backend Hexagonal, TDD, Docker com redes isoladas (backend↔front e backend↔banco separadas), `.dockerignore`, CI/CD com workflows.
6. **[MVP Gate] Condições mínimas para "já funciona" (comportamento observável):**
   - Consigo **criar uma conta** e fazer **login**.
   - **Cadastro um serviço** (nome + duração) na área web.
   - A plataforma me dá um **link de bot** do Telegram.
   - Abro o bot, **vejo os serviços** e peço os **dias disponíveis** (recebo um **calendário**).
   - **Escolho um serviço e marco um horário** pela conversa e o bot **confirma**.
   - Abro minha **área web** e vejo esse agendamento no **calendário**.

---

## Decisões fechadas

- [x] **Modelo do bot**: 1 bot da plataforma + deep link por conta (`t.me/SeuBot?start=conta123`).
- [x] **Stack**: Java + Spring Boot + JUnit / Next.js / PostgreSQL.
- [x] **Login na área web**: sim, já no MVP (cada dono vê só a própria conta).
- [x] **Multi-tenant**: isolamento por coluna `conta_id` em todas as tabelas (um banco só).
- [x] **Serviços**: o dono cria serviços (nome, duração, descrição, preço opcional); o agendamento referencia um serviço já criado; o bot lista os serviços ao cliente.
- [x] **Confirmação / identidade / cancelar**: auto-confirma (dono pode recusar depois) · identidade do Telegram basta · cliente cancela e remarca pelo bot.
