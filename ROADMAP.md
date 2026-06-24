# ROADMAP — gerenciador-atendimentos

**Data:** 2026-06-24
**Total de fases:** 7

---

## Como ler este roadmap

- **Critério de conclusão** = comportamento observável, não tarefa técnica.
- **Dependência** = qual fase precisa estar concluída antes.
- **Gate de arquitetura** = toda fase termina com a Revisão de Arquitetura registrada em `ARCH-REVIEWS.md` antes de abrir a próxima.
- **[HITL]** = você valida pelo comportamento antes de avançar. **[AFK]** = o agente fecha sozinho.
- Regra: tarefa que muda contrato de módulo ou afeta vários módulos → **[HITL]**.

O **MVP Gate** do PRD é atingido ao final da **Fase 6**. A Fase 7 completa o conjunto de features do MVP (cancelar/remarcar pelo bot).

---

## Fases

### Fase 1 — Esqueleto ponta a ponta
**Objetivo:** ter o sistema rodando vazio, com os containers e redes isoladas de pé.
**Critério de conclusão:**
> Subo `docker-compose up`, acesso o frontend Next.js, o backend responde em `/health`, o backend conecta no Postgres (Flyway roda a migração inicial) e o frontend **não** enxerga o banco.
**Tarefas para o agente:**
1. [AFK] Esqueleto Maven (backend) com pacotes Hexagonal vazios + endpoint `/health`.
2. [AFK] Esqueleto Next.js (App Router + TS + Tailwind, `output: 'standalone'`) com uma página simples.
3. [AFK] Migração Flyway inicial (extensão/baseline) + `application.yml` (datasource via variáveis do compose).
4. [AFK] Workflow de CI (GitHub Actions, `.github/workflows/ci.yml`): roda os testes do backend antes do build das imagens. (O agente cria o arquivo; o `git push` é responsabilidade do usuário.)
5. [HITL] Validar a subida ponta a ponta com `docker-compose up`.

> **Já criados nesta sessão** (antecipados, fora do fluxo de fase): `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `.dockerignore` de cada e `.env.example`. Falta o esqueleto de código que eles compilam.

**Dependência:** nenhuma
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 2 — Conta + login
**Objetivo:** dono cria conta e autentica na web.
**Critério de conclusão:**
> Crio uma conta (nome/email/senha), faço login, recebo um JWT e acesso uma área logada; uma conta não vê dados de outra.
**Tarefas para o agente:**
1. [HITL] Domínio `Conta` + geração de `bot_deep_link_token` (TDD).
2. [HITL] Use cases CriarConta/Login (ports/in) + ports/out de repositório.
3. [HITL] Adapter persistência (JPA + Flyway `conta`) + adapter web (Spring Security + JWT, BCrypt).
4. [HITL] Tela de signup/login no frontend.
**Dependência:** Fase 1
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 3 — Serviços + horários de atendimento
**Objetivo:** dono cadastra o que oferece e quando atende.
**Critério de conclusão:**
> Logado, crio um serviço (nome + duração [+ descrição/preço opcional]) e defino janelas de atendimento; ambos aparecem listados e ficam isolados por conta.
**Tarefas para o agente:**
1. [HITL] Domínio `Servico` e `HorarioAtendimento` (+ enum `DiaSemana`) com validações (TDD).
2. [HITL] Use cases CriarServico/ListarServicos/DefinirHorario + persistência (Flyway).
3. [HITL] Telas de serviços e horários no frontend.
**Dependência:** Fase 2
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 4 — Bot: deep link, listar serviços e disponibilidade
**Objetivo:** o cliente conversa com o bot da conta certa e vê o que está disponível.
**Critério de conclusão:**
> Abro o bot pelo link da conta (`?start=<token>`), vejo os serviços ativos e peço os dias/horários disponíveis, recebendo um calendário no Telegram (grade de 30 min, só horários futuros).
**Tarefas para o agente:**
1. [HITL] Domínio de **Disponibilidade/Slot** (grade − ocupados CONFIRMADO; serviço ocupa N fatias) — núcleo TDD.
2. [HITL] Adapter Telegram (long polling) + resolução do deep link → conta.
3. [HITL] Use cases ListarServicos (bot) e ConsultarDisponibilidade; teclados/calendário inline.
**Dependência:** Fase 3
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 5 — Bot: agendar com auto-confirmação e regra de sobreposição
**Objetivo:** o cliente fecha um agendamento e o bot confirma; o núcleo protege a agenda.
**Critério de conclusão:**
> Escolho um serviço e um horário pela conversa e o bot responde "tudo certo!"; uma tentativa que se sobrepõe a outro CONFIRMADO é recusada com mensagem clara.
**Tarefas para o agente:**
1. [HITL] Domínio `Agendamento` (+ enums `StatusAgendamento`, `OrigemAgendamento`) e regra de sobreposição `[inicio, fim)` — TDD primeiro.
2. [HITL] Use case AgendarConsulta (cria Cliente origem BOT se preciso) + idempotência de `callback_query`.
3. [HITL] Fluxo de confirmação no bot.
**Dependência:** Fase 4
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 6 — Web: calendário, recusar/cancelar e cadastro manual  ← **MVP Gate**
**Objetivo:** o dono enxerga e controla a agenda pela web.
**Critério de conclusão:**
> Na área web vejo o calendário com a quantidade de agendamentos por dia e a lista do dia; consigo recusar/cancelar um agendamento; cadastro um cliente manualmente (telefone + serviço); e copio o link do bot. **(Fecha o ciclo conta → bot → agendamento → visão na web.)**
**Tarefas para o agente:**
1. [HITL] Use case CalendárioDeAgendamentos (agregação por dia) + recusar/cancelar.
2. [HITL] Use case CadastrarClienteManual (+ enum `OrigemCliente`).
3. [HITL] Telas: calendário, detalhe do dia, cadastro manual, link do bot.
**Dependência:** Fase 5
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

### Fase 7 — Bot: cancelar e remarcar
**Objetivo:** o cliente gerencia o próprio agendamento pelo bot.
**Critério de conclusão:**
> Pelo bot, cancelo um agendamento (o horário volta a ficar livre) e remarco para outro horário válido (mesmo registro, novas datas); o reflexo aparece no calendário da web.
**Tarefas para o agente:**
1. [HITL] Use cases Cancelar/Remarcar (remarcar = update do mesmo registro, revalidando regras).
2. [HITL] Fluxo de cancelar/remarcar no bot.
**Dependência:** Fase 6
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md`

---

## Marcos de validação

| Após a Fase | Pergunta de validação |
|---|---|
| 1 | O esqueleto sobe e as redes estão realmente isoladas? |
| 3 | O dono consegue modelar o negócio dele (serviços + horários) sem ambiguidade? |
| 6 | O MVP resolve o problema do PRD (ciclo ponta a ponta)? |
| Final (7) | Eu usaria isso para gerenciar agendamentos de verdade? |
