# DECISIONS — Change Log de Decisões

Toda mudança estrutural gera uma entrada aqui (e atualiza o `ARCHITECTURE.md` no mesmo gatilho).

---

## 2026-06-25 — Stack e metodologia (fechamento do ADR)

**O que mudou:** definida a stack e a metodologia do projeto.
- Backend: Java 21 + Spring Boot 3.x (Maven), JUnit 5 + Mockito + AssertJ.
- Persistência: Spring Data JPA + PostgreSQL, migrações Flyway.
- Bot: TelegramBots (rubenlagus) em modo long polling.
- Auth web: Spring Security + JWT (stateless).
- Frontend: Next.js (App Router) + TypeScript.
- Orquestração: Docker Compose com redes isoladas; CI no GitHub Actions.
- Metodologia: Híbrido TDD+SDD (DDD-lite na linguagem), camada dominante TDD.

**Por que:** briefing exige Hexagonal + TDD; stack escolhida é o caminho de menor atrito
para esse estilo e para o histórico do usuário. TDD lidera porque a dor maior está na lógica
de agendamento e na integração frágil com o Telegram.

**Alternativa descartada:** Node/NestJS, Gradle, webhook do Telegram, sessão por cookie,
MySQL/SQLite, React SPA/Remix, Modelo B do bot (token por conta). Detalhe no ADR.

**Impacto:** define estrutura de pacotes Hexagonal, build Maven, schema multi-tenant por
`conta_id`, e os gates de fitness function.

**Como reverter:** trocar a tecnologia da camada afetada no ADR + ARCHITECTURE e regenerar
o esqueleto correspondente (nenhum código de produção ainda escrito).

---

## 2026-06-26 — telegrambots adiado para a Fase 4

**O que mudou:** removida a dependência `org.telegram:telegrambots:6.9.7.1` do `pom.xml` na
Fase 1.

**Por que:** a versão 6.x é pré-Jakarta e arrasta JAXB legado (`javax.xml.bind`), que quebra
a subida do contexto no Spring Boot 3 (`ClassNotFoundException: javax.xml.bind.annotation.XmlElement`).

**Alternativa descartada:** forçar `jaxb-api`/`jakarta.xml.bind` legado no classpath — gambiarra
que mascara a incompatibilidade.

**Impacto:** o adapter do bot (Fase 4) usará a linha 7.x (`telegrambots-longpolling` /
`telegrambots-client`), já Jakarta-compatível. Nenhuma regra de domínio afetada.

**Como reverter:** readicionar a dependência na versão correta ao iniciar a Fase 4.

---

## 2026-06-25 — Regras de domínio resolvidas no grill

**O que mudou:** fechadas 3 ambiguidades do domínio.
- Slot: encadeado pela duração do serviço a partir do início da janela.
- Disponibilidade: bot mostra só slots livres (futuros + sem overlap com CONFIRMADOS).
- Remarcar: move o mesmo registro (atualiza início/fim, mantém CONFIRMADO).
- Calendário do bot: horizonte de 30 dias.

**Por que:** evitar que o agente interprete esses termos sozinho no SDD.

**Alternativa descartada:** grade fixa de horários; remarcar como cancelar+criar; janelas de
7/60 dias.

**Impacto:** define o cálculo de disponibilidade e o comportamento do bot. Registrado no
`CONTEXT.md`.

**Como reverter:** reabrir o item no CONTEXT.md e ajustar o serviço de disponibilidade.
