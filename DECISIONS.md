# DECISIONS — gerenciador-atendimentos

> Registro de decisões estruturais. Não é changelog de código — é registro de intenção.
> Novas entradas sempre no topo.

---

## Entradas

### 2026-06-24 — CI/CD mantido (GitHub Actions) + escopo da restrição de git

**O que mudou:** Confirmado que o projeto terá **CI/CD com GitHub Actions** (`.github/workflows/ci.yml`), ancorado na **Fase 1** do ROADMAP (rodar testes do backend antes do build das imagens). Também criados os arquivos Docker (compose, Dockerfiles, .dockerignore, .env.example) antecipadamente.
**Por que:** O usuário esclareceu que a restrição "não usar GitHub" é, na verdade, **apenas o agente não executar `git add`/`commit`/`push`** — ele versiona manualmente. GitHub como plataforma e CI/CD são permitidos, então o requisito de CI/CD do briefing some sem conflito.
**Alternativa descartada:** Verificação só local sem CI remoto; CI em ferramenta não-GitHub; remover CI/CD do MVP.
**Impacto:** O agente pode criar/editar workflows, mas nunca dá push (isso é do usuário). O comentário "testes rodam no CI" no `backend/Dockerfile` fica válido.
**Como reverter:** Remover `.github/workflows/` e a tarefa de CI da Fase 1.

### 2026-06-24 — Bootstrap recomeçado do zero nesta pasta

**O que mudou:** Geração do conjunto de documentos de bootstrap (PRD, CONTEXT, ADR, ARCHITECTURE, SDD, ROADMAP, DECISIONS) a partir do `BRIEFING-projeto-agendamento-bot.md`.
**Por que:** A pasta `d:\ClaudeCodeReferencias\gerenciador-atendimentos` só continha o BRIEFING e nenhum commit, embora houvesse registro de um bootstrap anterior (2026-06-20) feito em outro lugar/perdido. O usuário optou por recomeçar do zero aqui.
**Alternativa descartada:** Procurar/reaproveitar os docs antigos em outra pasta do disco.
**Impacto:** Toda decisão de domínio/stack passa a viver nestes `.md`; o BRIEFING segue como fonte de intenção original.
**Como reverter:** Apagar os `.md` gerados; o BRIEFING permanece intacto.

### 2026-06-24 — Stack definida por consenso (Stack Grill)

**O que mudou:** Fixadas as sub-decisões de stack: **Maven, Java 21, Spring Data JPA/Hibernate, Flyway, TelegramBots (rubenlagus)+starter com long polling, Spring Security + JWT stateless (BCrypt), Next.js App Router + TS + Tailwind**. Detalhes e alternativas no ADR.
**Por que:** O briefing fixava só o alto nível (Java/Spring/JUnit/Next/Postgres); o usuário pediu explicitar e confirmar as sub-decisões em vez de assumir em silêncio.
**Alternativa descartada:** Gradle, Java 17, Spring Data JDBC, jOOQ, Liquibase, WebClient cru, webhook, sessão server-side, Pages Router, shadcn/ui no MVP (ver tabela "Decisões Descartadas" do ADR).
**Impacto:** Define dependências do `pom.xml`, do `package.json` e o desenho dos adapters.
**Como reverter:** Reabrir o Stack Grill para a camada específica e atualizar ADR + ARCHITECTURE + DECISIONS.

### 2026-06-24 — Regras de domínio: slots, fuso, remarcar, sobreposição

**O que mudou:** (1) Horários por **grade fixa** de slots (`granularidade_min`, 30 no MVP), com a **duração vinda do serviço** (variável) ocupando N fatias; (2) fuso **fixo America/Sao_Paulo** (persistir UTC); (3) **remarcar = update do mesmo registro**; (4) sobreposição: só **CONFIRMADO** bloqueia, intervalo **`[inicio, fim)`** meio-aberto.
**Por que:** Pontos ambíguos no briefing que viram código (lógica de disponibilidade, modelagem do agendamento). Resolvidos no Domain Grill; o usuário reforçou que a duração é por serviço (variável), confirmando que a grade governa só o início.
**Alternativa descartada:** Encaixe de início pela duração do serviço; fuso por conta; remarcar como cancelar+criar; fim inclusivo / CONCLUIDO bloqueando.
**Impacto:** Define o cálculo de disponibilidade, o schema (`conta.granularidade_min`, sem campo de fuso) e a operação de remarcar.
**Como reverter:** Atualizar CONTEXT.md + SDD e ajustar os use cases de disponibilidade/remarcação.
