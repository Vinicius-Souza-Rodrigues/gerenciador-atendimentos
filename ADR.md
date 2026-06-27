# ADR — Plataforma de Agendamento por Bot (Telegram)

**Data:** 2026-06-25
**Baseado no PRD:** v1.0
**Lê também:** `CONTEXT.md` (glossário canônico)

---

## Núcleo do Domínio

- [ ] Dados (armazenamento, transformação, consulta)
- [ ] Fluxo (processo, pipeline, automação)
- [x] **Regras de negócio** (validações, cálculos, decisões)
- [ ] UI/Experiência (interação, visualização)

> O centro é a **lógica de agendamento**: gerar slots, calcular disponibilidade, impedir
> overlap, garantir horário futuro e dentro da janela, isolar por `conta_id`. Dados e UI
> existem para servir essas regras. A integração com o Telegram é um adapter frágil que
> orbita esse núcleo.

---

## Complexidade de Estado

- [ ] Simples
- [x] **Moderada** — múltiplos módulos, estado compartilhado (agenda por conta), regra de
  não-sobreposição com potencial de concorrência (dois clientes tentando o mesmo slot).
- [ ] Alta

> Não é distribuída, mas a invariante de overlap + multi-tenant + confirmação automática
> exige cuidado transacional. Fica em "moderada" pela concorrência localizada.

---

## Ciclo de Vida Esperado

- [ ] MVP descartável
- [x] **Produto de longo prazo** — vai crescer (WhatsApp, faturamento, multi-profissional,
  lembretes já estão mapeados como pós-MVP).
- [ ] Biblioteca/SDK

---

## Consumidor

- [ ] Solo (só eu)
- [x] **Usuários externos** — SaaS multi-conta; donos de conta + clientes finais.
- [ ] Equipe pequena

> Desenvolvimento é solo; o **produto** é para terceiros. Isso eleva a régua de validação
> (detalhada) sem exigir cerimônia de equipe.

---

## Decisão de Metodologia

**Escolha:** Híbrido — **TDD + SDD**, com linguagem ubíqua de DDD-lite (vinda do `CONTEXT.md`).

**Justificativa:**
> O briefing exige Hexagonal + TDD, e isso casa com o núcleo: as regras invioláveis
> (overlap, disponibilidade, slot a partir da duração) são testáveis sem tocar
> Telegram/banco/HTTP. O **SDD** dá contratos claros de módulo (ports) e escopo negativo —
> bom para solo. O **DDD** entra só como *linguagem* (glossário do CONTEXT.md vira nomes de
> módulo/campo); não adotamos o tático pesado (aggregates/repositories cerimoniais além do
> necessário).

**Camada dominante (em conflito):** **TDD**.
> Resposta às 3 perguntas de desempate:
> 1. O que dói mais se quebrar? → a **lógica de agendamento** (overlap/disponibilidade) e o
>    **contrato com o Telegram** (integração frágil) — ambos pedem teste.
> 2. Dá para testar manualmente a regra? → parcialmente; overlap e geração de slots têm
>    muitos casos de borda → teste automatizado é mais confiável.
> 3. Solo ou equipe? → solo.
> Logo, quando SDD e TDD divergirem, **TDD lidera**: nenhuma regra de domínio entra sem teste.

---

## Stack Decidida

| Camada | Tecnologia | Motivo |
|---|---|---|
| Backend | **Java 21 + Spring Boot 3.x** | Hexagonal + TDD muito maduros; histórico do usuário (JUnit). LTS. |
| Build backend | **Maven** | Confirmado pelo usuário; usado na tentativa anterior; estável no ecossistema Spring. |
| Testes backend | **JUnit 5 + Mockito + AssertJ** | Padrão Spring; mocka ports no domínio sem subir contexto. |
| Persistência | **Spring Data JPA + PostgreSQL** | Multi-tenant por coluna `conta_id`; adapter de persistência converte Entity↔Domain. |
| Migrações | **Flyway** | Versionamento de schema (`V1__baseline.sql`); reprodutível em CI/Docker. |
| Bot Telegram | **TelegramBots (rubenlagus) — LongPolling** | Long polling (decidido): só internet de saída, sem URL pública/proxy. |
| Auth web | **Spring Security + JWT (stateless)** | Confirmado; combina com API REST stateless + Next.js. |
| Frontend | **Next.js (App Router) + TypeScript** | Área web; SSR/rotas; ecossistema React maduro. |
| Banco | **PostgreSQL** | Relacional, transações para a invariante de overlap; um banco só (multi-tenant por coluna). |
| Orquestração | **Docker Compose** com redes isoladas | `frontend-net` (front↔back) e `backend-net` (back↔db) separadas; db sem porta no host. |
| CI/CD | **GitHub Actions** | Testes no pipeline antes do build das imagens. |

---

## Decisões Descartadas

| Opção | Motivo da rejeição |
|---|---|
| **Node.js / NestJS** (backend) | Hexagonal + TDD pedidos casam melhor com Java/Spring maduro; histórico do usuário é JUnit. |
| **Gradle** (build) | Usuário optou por Maven (familiaridade + paridade com tentativa anterior). |
| **Webhook do Telegram** | Exigiria proxy na 443 + domínio + HTTPS; atrito desnecessário no MVP. Long polling basta. |
| **Sessão por cookie** (auth) | JWT stateless escolhido por simplicidade de escala e fit com SPA/Next.js. |
| **MySQL / SQLite** (banco) | Postgres tem melhor suporte transacional/constraints para a invariante de overlap e é o padrão do destino de produção. |
| **React SPA puro / Remix** (front) | Next.js dá roteamento + SSR prontos e é o caminho de menor atrito citado no briefing. |
| **Modelo B do bot** (token por conta) | Atrito de cada usuário criar bot no BotFather; Modelo A (deep link) entrega o "link pra compartilhar" de graça. |

---

## Fitness Functions

> Declaradas aqui, checadas no gate de cada fase (`ARCH-REVIEWS.md`). Começam sob checagem
> do agente; graduam para CI quando provarem valor.

| Fitness Function | Característica protegida | Como checar |
|---|---|---|
| Domínio não importa de Spring/JPA/Telegram | Isolamento do núcleo Hexagonal | grep de imports em `domain/**` (sem `org.springframework`, `jakarta.persistence`, `telegram`) |
| Domínio sem anotações de infra (`@Entity`, `@Component`, `@Autowired`) | Pureza do domínio | grep de anotações em `domain/**` |
| Application enxerga só ports (interfaces) | Inversão de dependência | nenhum import de `*Impl`/adapters concretos em `application/**` |
| Injeção via construtor (sem `@Autowired` em campo) | Testabilidade | grep `@Autowired` em campos |
| Enums em arquivos próprios (sem inner enum) | Regra de código do briefing | grep `enum ` aninhado dentro de classe |
| Domínio ↔ DTO só em adapters | Camadas não vazam tipos de domínio | DTOs (`*Request`/`*Response`) não aparecem em `domain/**`; conversão fora do domínio |
| Testes de domínio rodam sem Spring context | TDD do núcleo | testes em `domain/**` não usam `@SpringBootTest` |
| Frontend não alcança o banco | Isolamento de rede Docker | `db` não está em `frontend-net` no `docker-compose.yml` |
| Toda tabela tem `conta_id` | Isolamento multi-tenant | revisar migrações: PKs de negócio com `conta_id` |

---

## Metodologia registrada (Etapa 3 — Classifier)

Híbrido **TDD+SDD (DDD-lite na linguagem)**, camada dominante **TDD**. Ver seção
"Decisão de Metodologia" acima. Registrado também no `DECISIONS.md`.
