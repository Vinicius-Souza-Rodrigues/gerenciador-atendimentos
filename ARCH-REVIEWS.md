# ARCH-REVIEWS — Revisões de Arquitetura por Fase

Registro datado dos gates de fase do `ROADMAP.md`. Cada fase concluída gera uma entrada aqui
**antes** de abrir a próxima. Mantém o `ARCHITECTURE.md` como snapshot limpo.

Cada revisão checa 4 eixos: (1) drift contra o ADR, (2) crescimento de complexidade/
acoplamento, (3) dependências novas não justificadas, (4) fitness functions do ADR.

Se um eixo falha → protocolo de drift (apresentar opções, aguardar decisão) → registrar em
`DECISIONS.md` + atualizar `ARCHITECTURE.md`.

---

## 2026-06-26 — Gate da Fase 1 (Fundação)

**Eixo 1 — Drift vs ADR:** ok. Estrutura Hexagonal + Next.js + Docker conforme ADR/SDD.
**Eixo 2 — Complexidade/acoplamento:** ok. Sem regra de negócio ainda; só esqueleto + health.
**Eixo 3 — Dependências novas:** uma divergência **registrada** — `telegrambots 6.x` removido do
`pom` por arrastar JAXB legado (`javax.*`) incompatível com Spring Boot 3 (Jakarta). Será
adicionado na Fase 4 em versão Jakarta-compatível (telegrambots 7.x / longpolling). Ver DECISIONS.
**Eixo 4 — Fitness functions:** `frontend` fora de `backend-net` ✅ (compose); `db` sem porta no
host ✅; healthcheck no db + `depends_on: service_healthy` ✅; build/teste verde via `mvnw`.

**Resultado:** liberado para a Fase 2.

---

## 2026-06-26 — Gate da Fase 2 (Núcleo de agendamento)

**Eixo 1 — Drift vs ADR:** ok. Domínio puro, sem infra; termos do CONTEXT.md respeitados.
**Eixo 2 — Complexidade/acoplamento:** ok. Domínio sem dependências externas; cálculo isolado.
**Eixo 3 — Dependências novas:** nenhuma.
**Eixo 4 — Fitness functions (grep):**
- Domínio não importa Spring/JPA/Telegram ✅
- Domínio sem `@Entity`/`@Component`/`@Service`/`@Autowired` ✅
- Enums todos top-level (arquivo próprio) ✅
- Testes de domínio sem `@SpringBootTest` ✅ (só `contextLoads` usa Spring)
- 24 testes verdes (BUILD SUCCESS) cobrindo slot encadeado, janela, futuro, overlap, mesma-conta.

**Resultado:** liberado para a Fase 3.

---

## 2026-06-26 — Gate da Fase 3 (Conta, login e serviços)

**Eixo 1 — Drift vs ADR:** ok. Hexagonal respeitado: ports in/out, services na aplicação,
adapters web/persistência. Auth Spring Security + JWT stateless conforme decidido.
**Eixo 2 — Complexidade/acoplamento:** ok. Services finos delegando a ports; sem regra de
negócio em controller/adapter.
**Eixo 3 — Dependências novas:** nenhuma além das já previstas (jjwt já estava no pom).
**Eixo 4 — Fitness functions (grep + runtime):**
- Domínio puro (sem Spring/JPA/Telegram) ✅
- Application não importa adapters/`*Impl` e não importa web/jpa ✅
- Injeção só por construtor (sem `@Autowired` em campo) ✅
- Toda tabela com `conta_id` (migração V1) ✅
- 32 testes verdes (domínio + services com mocks).
- **Runtime (Docker):** `/api/servicos` sem token → 401; signup/login públicos; CRUD com token;
  **isolamento multi-tenant confirmado** (2ª conta vê lista vazia); frontend (login/signup/painel) 200.

**Endpoints públicos confirmados:** `/health`, `/api/auth/signup`, `/api/auth/login`. Resto exige JWT.

**Resultado:** liberado para a Fase 4 (bot de Telegram).

<!--
Modelo de entrada:

## [DATA] — Gate da Fase N

**Eixo 1 — Drift vs ADR:** ok / desvio: ...
**Eixo 2 — Complexidade/acoplamento:** ok / ...
**Eixo 3 — Dependências novas:** nenhuma / justificada: ...
**Eixo 4 — Fitness functions:** (lista com pass/fail)

**Resultado:** liberado para a Fase N+1 / bloqueado por ...
-->
