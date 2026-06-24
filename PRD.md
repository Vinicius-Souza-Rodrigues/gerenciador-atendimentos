# PRD — Plataforma de Agendamento por Bot (gerenciador-atendimentos)

**Data:** 2026-06-24
**Versão:** 1.0
**Fonte:** `BRIEFING-projeto-agendamento-bot.md`

---

## Problema

Pessoas e estabelecimentos que marcam horário (consultas, serviços, atendimentos)
gerenciam isso na mão — DM, telefone, papel. Resultado: esquecimento, conflito de
horário e nenhuma visão consolidada da agenda.

A plataforma automatiza o agendamento via **bot de Telegram** e centraliza a visão
numa **área web** própria de cada conta.

---

## Usuários

- [ ] Só eu
- [x] Outras pessoas
- [ ] API pública / terceiros

Dois atores:

| Ator | Quem é | Como interage |
|---|---|---|
| **Dono da conta** | Pessoa comum ou estabelecimento (é o *tenant*) | Área web: serviços, calendário, link do bot, cadastro manual |
| **Cliente final** | Quem quer marcar horário | Bot de Telegram, pelo deep link que o dono compartilha |

---

## Menor uso com valor real (MVP)

O ciclo **conta → bot → agendamento → visão na web** funcionando ponta a ponta:

1. Criar uma **conta** e fazer **login** na área web.
2. **Cadastrar um serviço** (nome + duração).
3. A plataforma fornece um **link do bot** do Telegram (deep link por conta).
4. No bot, o cliente **vê os serviços** e pede os **dias disponíveis** (recebe um calendário).
5. O cliente **escolhe um serviço e marca um horário** por conversa; o bot **confirma na hora**.
6. O dono abre a **área web** e vê esse agendamento no **calendário**.

Inclui ainda: cancelar/remarcar pelo bot, cadastro manual de cliente na web, e o dono
poder recusar/cancelar um agendamento pela web.

---

## Fora do escopo (agora)

- **WhatsApp** e outras plataformas além do Telegram (evolução futura via Evolution API).
- **Dashboard de faturamento** (somatório, relatórios de serviços pagos) — pós-MVP, domínio financeiro BR.
- **Lembrete automático** (ex.: 1h antes da consulta).
- **Vários profissionais por conta** (agendas separadas dentro de um estabelecimento) — MVP é **uma agenda por conta**.
- **Espaço de opções extras** configuráveis por conta.
- **Cobrança/pagamento**: o serviço tem preço **opcional**, mas é só cadastro — sem cobrança no MVP.

---

## Restrições conhecidas

- **Backend Hexagonal (Ports & Adapters)** — núcleo de domínio isolado; bot, web e banco são adapters.
- **TDD** — domínio testável sem tocar Telegram/banco/HTTP.
- **Stack (alto nível, fixa no briefing):** Java + Spring Boot + JUnit / Next.js / PostgreSQL.
  *(Sub-decisões — build tool, persistência, migrations, lib do Telegram, auth, polling×webhook — a confirmar no Stack Grill.)*
- **Multi-tenant por coluna `conta_id`** em todas as tabelas (um banco só).
- **Docker com redes isoladas**: `frontend-net` (frontend↔backend) e `backend-net` (backend↔db) separadas; db não exposto ao host em produção.
- **`.dockerignore`** por serviço, multi-stage build, healthcheck no db, volume nomeado, `.env` para segredos.
- **CI/CD**: testes rodam no pipeline antes do build das imagens.
- **Modelo do bot: A** — 1 bot da plataforma + deep link por conta (`t.me/SeuBot?start=<token>`).

## Dependências Externas

- **Telegram Bot API** (MVP) — dependência externa principal. O contrato (o que envia, o que
  recebe, comportamento em falha) será documentado no SDD.
  - O que controlo: 1 token de bot da plataforma, a lógica de roteamento por deep link.
  - O que vem de fora: updates de mensagens/callbacks, entrega das respostas.
- **WhatsApp Business / Evolution API** — futuro, fora do MVP.

---

## Critério de sucesso

Consigo, sem ler código:

- Criar conta + login na web.
- Cadastrar um serviço e vê-lo na lista.
- Copiar o link do bot, abri-lo no Telegram, ver os serviços e pedir os dias disponíveis (calendário).
- Escolher serviço + horário pela conversa e receber a confirmação do bot.
- Ver esse agendamento no calendário da área web.
- Cancelar/remarcar pelo bot e ver o reflexo na web.
