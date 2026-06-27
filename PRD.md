# PRD — Plataforma de Agendamento por Bot (Telegram)

**Data:** 2026-06-25
**Versão:** 1.0
**Fonte:** `BRIEFING-projeto-agendamento-bot.md` (calibração + PRD já respondidos)

---

## Problema

Pessoas e estabelecimentos que marcam horário (consultas, serviços, atendimentos)
gerenciam isso na mão — DM, telefone, papel. Resultado: esquecimento, conflito de
horário e nenhuma visão consolidada da agenda.

A plataforma automatiza o agendamento via **bot de Telegram** (para o cliente final)
e centraliza a visão numa **área web** (para o dono da conta).

---

## Usuários

- [ ] Só eu
- [x] Outras pessoas
- [ ] API pública / terceiros

São **dois atores**:

| Ator | Quem é | Como interage |
|---|---|---|
| **Dono da conta** | Pessoa comum ou estabelecimento (é o *tenant*) | Área **web**: serviços, calendário, link do bot, cadastro manual. Vê só a própria conta. |
| **Cliente final** | Quem quer marcar horário | **Bot de Telegram**, via deep link que o dono compartilha. |

---

## Menor uso com valor real (MVP)

O ciclo **conta → bot → agendamento → visão na web** funcionando ponta a ponta:

1. Crio uma **conta** e faço **login**.
2. Cadastro um **serviço** (nome + duração) na área web.
3. A plataforma me dá um **link de bot** do Telegram (deep link por conta).
4. Abro o bot, **vejo os serviços** e peço os **dias disponíveis** (recebo um **calendário**).
5. **Escolho um serviço e marco um horário** pela conversa; o bot **confirma na hora**.
6. Abro minha **área web** e vejo esse agendamento no **calendário**.

Funcionalidades do núcleo:

1. Criar conta + login (self-service; cada dono só vê a própria conta).
2. Provisionar o bot — deep link por conta (`t.me/SeuBot?start=<token>`).
3. Área web — gerenciar serviços (nome, duração, descrição, preço opcional).
4. Bot — listar serviços da conta.
5. Bot — consultar disponibilidade (responde com calendário no Telegram).
6. Bot — agendar por conversa (serviço + horário → valida → confirma automático).
7. Bot — cancelar / remarcar pelo próprio bot.
8. Área web — calendário de agendamentos (dias com marcações e quantidade por dia).
9. Área web — link do bot (copiar para compartilhar).
10. Área web — cadastro manual de cliente (telefone + serviço desejado).

---

## Fora do escopo (agora)

- **WhatsApp** e outras plataformas além do Telegram (Evolution API fica como evolução).
- **Dashboard de faturamento** / cobrança (preço é só campo cadastrado, sem cobrança).
- **Lembrete automático** 1h antes do horário.
- **Vários profissionais por conta** (uma agenda por conta no MVP).
- **Opções extras configuráveis** por conta.
- **Modelo B do bot** (cada conta cria o próprio bot no BotFather).

---

## Restrições conhecidas

- **Arquitetura Hexagonal** (Ports & Adapters) — núcleo de domínio isolado; bot, web e
  banco são adapters.
- **TDD** — domínio testável sem tocar Telegram/banco/HTTP.
- **Docker com redes isoladas**: `frontend-net` (frontend ↔ backend) e `backend-net`
  (backend ↔ db) separadas; o frontend não enxerga o banco. `.dockerignore` por serviço.
- **CI/CD com workflows** — testes rodam no pipeline antes do build das imagens.
- **Multi-tenant por coluna** `conta_id` em todas as tabelas (um banco só).
- **Stack decidida:** Java + Spring Boot + JUnit / Next.js / PostgreSQL.
- **Telegram (MVP):** Bot API via **long polling** (sem URL pública/proxy).
- **Auth web:** Spring Security + **JWT** (stateless).
- **Build backend:** Maven.

### Dependências Externas

| Sistema | Papel | O que controlo | O que vem dele |
|---|---|---|---|
| **Telegram Bot API** | Canal do cliente final (MVP) | Token do bot da plataforma; lógica de conversa | Updates (mensagens, callbacks de calendário), identidade do usuário (telegram_user_id, nome/username) |
| WhatsApp / Evolution API | Canal futuro (pós-MVP) | — | — |

> Identidade do cliente pelo bot = conta do Telegram (nome/username). O bot **não pede
> telefone**; telefone só aparece no cadastro manual da web.

---

## Critério de sucesso

O MVP "já funciona" quando, observando o comportamento, eu consigo:

- Criar uma conta e fazer login.
- Cadastrar um serviço (nome + duração) na área web.
- Receber um link de bot do Telegram.
- Abrir o bot, ver os serviços e pedir os dias disponíveis (receber um calendário).
- Escolher um serviço e marcar um horário pela conversa, e o bot confirmar.
- Abrir a área web e ver esse agendamento no calendário.
