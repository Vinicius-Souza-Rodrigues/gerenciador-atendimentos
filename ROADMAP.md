# ROADMAP — Plataforma de Agendamento por Bot (Telegram)

**Data:** 2026-06-25
**Total de fases:** 6
**Baseado em:** PRD v1.0, ADR v1.0, SDD, CONTEXT

---

## Como ler este roadmap

- **Critério de conclusão** = comportamento observável, não tarefa técnica.
- **Dependência** = qual fase precisa estar concluída antes.
- **Gate de arquitetura** = toda fase termina com a Revisão de Arquitetura registrada em
  `ARCH-REVIEWS.md` antes de abrir a próxima.
- **[AFK]** = o agente fecha sozinho. **[HITL]** = exige sua validação por comportamento
  (Validation Checklist obrigatória). Regra: muda contrato de módulo / afeta vários módulos → HITL.
- **Uma tarefa por vez.** Qualquer incerteza de lógica → o agente PARA e pergunta.

---

## Fases

### Fase 1 — Fundação (esqueleto + Docker + CI)

**Objetivo:** ter o ambiente de pé e o pipeline rodando, sem regra de negócio ainda.

**Critério de conclusão:**
> Eu rodo `docker compose up` e os containers sobem; o backend responde em `/health`; o
> frontend abre uma página inicial; e o push dispara o CI que roda os testes (mesmo vazios).

**Tarefas para o agente:**
1. [AFK] Esqueleto de pastas backend (Hexagonal) + frontend (Next.js) conforme SDD.
2. [AFK] `pom.xml` (Spring Boot, JPA, Security, Flyway, TelegramBots, JUnit/Mockito/AssertJ).
3. [HITL] `docker-compose.yml` com `frontend-net` e `backend-net` isoladas, `db` sem porta no host, healthcheck no db + `depends_on: service_healthy`.
4. [AFK] `.dockerignore` por serviço, `.env.example`, multi-stage `Dockerfile`s.
5. [AFK] `HealthController` (`/health`) + página inicial Next.js.
6. [HITL] `.github/workflows/ci.yml` rodando `mvn test` antes do build das imagens.

**Dependência:** nenhuma
**Gate de arquitetura:** [ ] Revisão registrada em `ARCH-REVIEWS.md` (checar isolamento de rede + fitness functions estruturais)

---

### Fase 2 — Núcleo de agendamento (domínio puro, TDD)

**Objetivo:** as regras invioláveis existem e estão testadas, sem tocar banco/Telegram/HTTP.

**Critério de conclusão:**
> Rodo os testes de domínio e vejo verde cobrindo: geração de slot encadeado, slot só dentro
> da janela, slot futuro, não-sobreposição de CONFIRMADOS, `fim = inicio + duração`, e
> isolamento por `conta_id`.

**Tarefas para o agente:**
1. [HITL] Entidades de domínio (`Conta`, `Servico`, `Cliente`, `HorarioAtendimento`, `Agendamento`) + enums em arquivos próprios (`StatusAgendamento`, `OrigemCliente`, `OrigemAgendamento`, `DiaSemana`).
2. [HITL] `Slot` + `CalculadoraDisponibilidade` (encadeado pela duração, filtra livres, horizonte 30 dias, `Clock` injetável) — TDD primeiro.
3. [HITL] Regra de não-sobreposição e validações (futuro, dentro da janela) com testes de borda.

**Dependência:** Fase 1
**Gate de arquitetura:** [ ] Revisão registrada (domínio sem imports de infra; testes sem `@SpringBootTest`)

---

### Fase 3 — Conta, login e serviços (área web)

**Objetivo:** o dono cria conta, loga e gerencia serviços e horários pela web.

**Critério de conclusão:**
> Eu faço signup, login (recebo um JWT), cadastro um serviço (nome + duração) e ele aparece
> na lista; defino meus horários de atendimento; e vejo o link do meu bot.

**Tarefas para o agente:**
1. [HITL] Ports in/out + services: `CriarContaUseCase`, `AutenticarUseCase`, `GerenciarServicoUseCase`, `GerenciarHorarioUseCase`, `ResolverContaPorTokenUseCase`.
2. [HITL] Adapter persistência (JPA entities + mappers Entity↔Domain) + migração Flyway `V1__baseline.sql`.
3. [HITL] Adapter web: endpoints `/auth/*`, `/servicos`, `/horarios`, `/conta/bot-link` + Spring Security + JWT.
4. [HITL] Telas Next.js: signup/login, lista/criar serviço, horários, link do bot.

**Dependência:** Fase 2
**Gate de arquitetura:** [ ] Revisão registrada (inversão de dependência; DTOs não vazam domínio; `conta_id` em toda tabela)

---

### Fase 4 — Bot de Telegram (agendar ponta a ponta)

**Objetivo:** o cliente agenda pelo bot via deep link.

**Critério de conclusão:**
> Abro o bot pelo link `t.me/SeuBot?start=<token>`, vejo os serviços da conta, peço os dias
> disponíveis (recebo um calendário), escolho serviço + horário e o bot confirma ("tudo
> certo!"). Consigo cancelar e remarcar pelo próprio bot.

**Tarefas para o agente:**
1. [HITL] Adapter in/telegram (long polling): roteamento de `/start <token>`, comandos e `callback_query`.
2. [HITL] Use cases do bot: `ListarServicosUseCase`, `ConsultarDisponibilidadeUseCase`, `AgendarUseCase`, `CancelarAgendamentoUseCase`, `RemarcarAgendamentoUseCase` (transação na invariante de overlap).
3. [HITL] Adapter out/telegram (envio de mensagem + teclado de calendário inline) + criação automática de `Cliente` origem BOT.

**Dependência:** Fase 3
**Gate de arquitetura:** [ ] Revisão registrada (adapter sem regra de negócio; falha de Telegram não derruba transação)

---

### Fase 5 — Visão na web (calendário + cadastro manual)

**Objetivo:** o dono enxerga e administra os agendamentos.

**Critério de conclusão:**
> Abro a área web e vejo no calendário o agendamento feito pelo bot (dias com quantidade de
> marcações); abro um dia e vejo a lista; cancelo um agendamento; e cadastro um cliente
> manualmente (telefone + serviço).

**Tarefas para o agente:**
1. [HITL] Use cases `VerAgendaUseCase`, `CadastrarClienteManualUseCase` + endpoints `/agenda`, `/agendamentos`, `/clientes/manual`.
2. [HITL] Tela de calendário web (dias + quantidade) e detalhe do dia com cancelar.
3. [AFK] Tela de cadastro manual de cliente.

**Dependência:** Fase 4
**Gate de arquitetura:** [ ] Revisão registrada (MVP Gate completo; sem drift de complexidade)

---

### Fase 6 — Endurecimento (CI/CD completo + isolamento verificado)

**Objetivo:** deixar o ciclo confiável e reprodutível.

**Critério de conclusão:**
> O pipeline roda todos os testes e builda as imagens; confirmo que o `frontend` não alcança
> o `db` (rede isolada); e há um teste de fluxo cobrindo "agendou no bot → aparece na web".

**Tarefas para o agente:**
1. [AFK] CI/CD: build das imagens após testes verdes; cache de dependências.
2. [HITL] Teste de integração/e2e do fluxo ponta a ponta (bot → web).
3. [AFK] Revisão de segredos (`.env` fora do git), README de execução.

**Dependência:** Fase 5
**Gate de arquitetura:** [ ] Revisão final registrada (todas as fitness functions; candidatas a graduar para CI)

---

## Marcos de validação

| Após a Fase | Pergunta de validação |
|---|---|
| 1 | O ambiente sobe e o pipeline roda — a fundação está sólida? |
| 3 | Consigo criar conta, logar e cadastrar serviço — o lado do dono entrega valor? |
| 4 | O cliente consegue agendar pelo bot de ponta a ponta? |
| 5 | O MVP resolve o problema do PRD (ciclo conta→bot→agendamento→web)? |
| Final | Eu usaria isso no dia a dia? |
