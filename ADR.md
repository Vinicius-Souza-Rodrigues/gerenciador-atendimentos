# ADR — gerenciador-atendimentos

**Data:** 2026-06-24
**Baseado no PRD:** v1.0
**Lê também:** CONTEXT.md (glossário canônico)

---

## Núcleo do Domínio

- [ ] Dados
- [ ] Fluxo
- [x] **Regras de negócio** (disponibilidade, sobreposição, multi-tenant, auto-confirmação)
- [ ] UI/Experiência

> O centro é a **agenda**: validar que um agendamento é válido (futuro, dentro da janela,
> sem sobreposição com outro CONFIRMADO, na mesma conta). O bot e a web são adapters dessa lógica.

---

## Complexidade de Estado

- [ ] Simples
- [x] **Moderada** — múltiplos módulos (conta, serviço, cliente, agendamento, horário) e um
  ponto quente de **concorrência**: dois clientes tentando o mesmo slot. Estado não é distribuído.
- [ ] Alta

---

## Ciclo de Vida Esperado

- [ ] MVP descartável
- [x] **Produto de longo prazo** — é um SaaS que deve crescer (WhatsApp, faturamento, múltiplos profissionais, lembretes).
- [ ] Biblioteca/SDK

---

## Consumidor

- [x] **Solo** (eu sou o único desenvolvedor) — porém com **usuários externos** (donos de conta na web, clientes no bot).
- [ ] Equipe pequena
- [ ] API pública

---

## Decisão de Metodologia

**Escolha:** **Híbrido — TDD dominante + DDD-lite + SDD**

**Justificativa:**
> O briefing fixa **Hexagonal + TDD**. O núcleo são regras invioláveis cujo erro é grave e
> difícil de pegar no olho (sobreposição, concorrência de slot) → **TDD** garante isso com
> testes de domínio puros, sem Spring. O domínio tem linguagem rica (ver CONTEXT.md) →
> **DDD-lite** dá a linguagem ubíqua e os agregados (Conta como raiz multi-tenant). Os
> contratos dos adapters (Telegram, HTTP, persistência) são claros → **SDD** especifica
> entrada/saída de cada um.

**Camada dominante (em conflito):** **TDD** — quando houver dúvida, escrever o teste do
domínio primeiro; o design segue o que o teste exige.

**3 perguntas de desempate (registro):**
1. O que dói mais se quebrar? → Empate prático: regra de negócio (agendamento duplicado é grave) e Telegram (produto fica inútil). Ambos exigem rede de testes → TDD.
2. Consigo testar a regra manualmente? → Não com confiança (concorrência/sobreposição) → favorece TDD automatizado.
3. Solo ou equipe? → Solo.

---

## Stack Decidida

> Decidida no Stack Grill (2026-06-24), por consenso, com viés anti-escopo (partir do mais boring).

| Camada | Tecnologia | Motivo |
|---|---|---|
| Linguagem backend | **Java 21 (LTS)** | LTS atual; records/pattern matching ajudam o domínio imutável; virtual threads disponíveis. |
| Framework backend | **Spring Boot 3.x** | Fixado no briefing; ecossistema maduro p/ Hexagonal + TDD. |
| Build | **Maven** | Mais boring e onipresente no ecossistema Spring; menos surpresa em CI. |
| Arquitetura | **Hexagonal (Ports & Adapters)** | Fixado no briefing; isola o domínio de bot/web/banco. |
| Testes | **JUnit 5** (+ Mockito, AssertJ) | Fixado no briefing; domínio testável sem infra. |
| Persistência | **Spring Data JPA / Hibernate** | Mais conhecido/produtivo; entities JPA ficam só no adapter, com mapeamento Entity↔Domain. |
| Migrations | **Flyway** | SQL puro versionado, transparente, alinhado a Postgres. |
| Banco | **PostgreSQL** | Fixado no briefing. |
| Bot Telegram | **TelegramBots (rubenlagus) + starter Spring**, **long polling** | Lib Java madura; polling dispensa HTTPS público/proxy no MVP. Fica atrás de um port. |
| Auth web | **Spring Security + JWT stateless** (senha com **BCrypt**) | Backend sem estado de sessão; front Next.js desacoplado. Só donos logam (clientes usam Telegram). |
| Frontend | **Next.js (App Router) + TypeScript + Tailwind** | Default moderno do Next; TS para tipos; Tailwind para estilo rápido. Lib de componentes adiada. |
| Infra | **Docker + docker-compose**, redes isoladas (`frontend-net`, `backend-net`) | Fixado no briefing; db não exposto ao host em produção. |
| CI/CD | **GitHub Actions** | Testes antes do build das imagens (TDD no pipeline). |

---

## Decisões Descartadas

| Opção | Motivo da rejeição |
|---|---|
| **Gradle** (build) | Maven é mais boring/onipresente no Spring; build mais previsível para um projeto solo. |
| **Java 17** | 21 é o LTS atual com records/virtual threads; sem motivo para ficar atrás. |
| **Spring Data JDBC** (persistência) | Mais leve e aderente a DDD, mas JPA é mais conhecido/produtivo; a disciplina de manter entities no adapter resolve o "vazamento" do ORM. |
| **jOOQ** (persistência) | Controle total de SQL não compensa a curva/geração de código num MVP solo. |
| **Liquibase** (migrations) | Flyway com SQL puro é mais simples e transparente para o escopo. |
| **WebClient cru** (Telegram) | Reimplementaria polling/teclados que a lib madura já entrega. |
| **Webhook** (transporte do bot) | Exige HTTPS público + proxy + domínio já no MVP; polling não precisa de nada disso. |
| **Sessão server-side** (auth) | JWT stateless escolhido por desacoplar o front e dispensar estado de sessão no backend. |
| **Pages Router** (Next.js) | App Router é o default moderno do framework. |
| **shadcn/ui no MVP** (frontend) | Lib de componentes é peça extra; adiada por viés anti-escopo (entra quando a UI justificar). |

---

## Fitness Functions

> Asserções objetivas checadas no gate de cada fase (ver `arch-review-guide.md`). Começam sob
> checagem do agente; graduam para CI quando provarem valor.

| Fitness Function | Característica protegida | Como checar |
|---|---|---|
| Domínio não importa de Spring/JPA/Telegram | separação de camadas (Hexagonal) | grep de imports na camada `domain` |
| Domínio e application dependem só de **ports** (interfaces), nunca de adapters concretos | inversão de dependência | grep por `*Impl`/`*Adapter` em domain/application |
| Cada `enum` em arquivo próprio (sem inner enum) | convenção do briefing | grep `enum ` dentro de classes |
| Classes de domínio não trafegam entre camadas (DTOs `*Request`/`*Response`/`*Command` na borda) | contrato de camadas | revisão dos controllers/adapters |
| Injeção por construtor; sem `@Autowired` em campo | testabilidade/inversão | grep `@Autowired` em campos |
| Toda tabela/entidade tem `conta_id` | isolamento multi-tenant | revisão das migrations e entities |
| Testes de domínio rodam sem contexto Spring (JUnit puro) | domínio testável isolado | grep `@SpringBootTest` em testes de `domain` |
