# CONTEXT — Glossário do Domínio

**Data:** 2026-06-25
**Origem:** Etapa 1.5 (Domain Grilling) do bootstrap. Termos canônicos para o SDD e o código.

> Regra: estes são os nomes oficiais. Módulos, tabelas e campos usam exatamente esta
> terminologia. Qualquer termo novo que vire código deve ser adicionado aqui antes.

---

## Atores

| Termo | Definição precisa | O oposto / não confundir com |
|---|---|---|
| **Dono da conta** | Pessoa ou estabelecimento que cria a conta e administra pela área web. É o *tenant*. | Não é o cliente final; não usa o bot para administrar. |
| **Cliente final** | Pessoa que agenda um horário pelo bot de Telegram (ou é cadastrada manualmente). | Não tem acesso à área web. |

---

## Entidades de domínio

### Conta
O *tenant*. Unidade de isolamento: todo dado pertence a uma `conta_id`.
- Campos: `id`, `nome`, `email` (login único), `senha_hash`, `bot_deep_link_token`, `criado_em`.
- `bot_deep_link_token`: o valor de `start=<token>` no link `t.me/SeuBot?start=<token>`.
- Tem **uma única agenda** (uma agenda por conta no MVP).

### HorarioAtendimento
A **disponibilidade configurada** da conta — as janelas em que ela atende.
- Campos: `id`, `conta_id`, `dia_semana` (SEG..DOM), `hora_inicio`, `hora_fim`.
- É **configuração**, não é horário livre. Pode haver mais de uma janela por dia.
- Não confundir com **disponibilidade calculada** (ver abaixo).

### Serviço
O que o dono oferece; define a duração do agendamento.
- Campos: `id`, `conta_id`, `nome`, `duracao_min`, `descricao`, `preco` (nullable), `ativo`, `criado_em`.
- `duracao_min`: tamanho do slot daquele serviço.
- `ativo`: se aparece na listagem do bot. Inativo não pode ser agendado.
- Todo agendamento referencia um serviço já criado.

### Cliente
Quem agenda. Vem de **2 origens**.
- Campos: `id`, `conta_id`, `nome`, `telegram_user_id` (nullable), `telefone` (nullable), `origem`, `criado_em`.
- `origem`: `BOT` (veio do Telegram, identificado por `telegram_user_id`) ou `MANUAL`
  (cadastrado na web, identificado por `telefone`).
- **Regra:** todo cliente tem `telegram_user_id` OU `telefone` (ao menos um).

### Agendamento
A marcação de um horário.
- Campos: `id`, `conta_id`, `cliente_id`, `servico_id`, `inicio`, `fim`, `preco_cobrado`
  (snapshot, nullable), `status`, `origem`, `criado_em`, `atualizado_em`.
- `fim` = `inicio` + `duracao_min` do serviço.
- `preco_cobrado`: snapshot do preço do serviço no momento da marcação (só registro, sem cobrança).
- `status`: `CONFIRMADO` / `CANCELADO` / `CONCLUIDO`.
- `origem`: `BOT` ou `MANUAL`.

---

## Conceitos calculados / de processo

| Termo | Definição precisa |
|---|---|
| **Slot** | Um horário candidato a agendamento. **Gerado encadeado pela duração do serviço**, a partir do `hora_inicio` da janela: passos de `duracao_min` (ex.: janela 09:00–12:00 + serviço 30min → 09:00, 09:30, 10:00...). Um slot só "cabe" se `inicio + duracao_min <= hora_fim` da janela. |
| **Disponibilidade calculada** | Conjunto de slots **livres** = slots gerados que (a) são futuros e (b) não se sobrepõem a nenhum `Agendamento` CONFIRMADO da conta. O bot mostra **apenas slots livres**. |
| **Dia disponível** | Dia (dentro da janela do calendário) que tem ≥1 slot livre para o serviço escolhido. |
| **Janela do calendário (bot)** | Horizonte de consulta de disponibilidade: **do dia atual até 30 dias à frente**. |
| **Confirmação automática** | Ao agendar pelo bot, o status nasce `CONFIRMADO` direto (sem aprovação prévia do dono). O dono pode **recusar/cancelar depois** pela web (→ `CANCELADO`). |
| **Remarcar** | O cliente muda o horário de um agendamento existente pelo bot. **Move o mesmo registro**: atualiza `inicio`/`fim` (e `atualizado_em`), mantém `status = CONFIRMADO` e o mesmo `id`. Revalida overlap e disponibilidade. |
| **Cancelar** | Muda o `status` do agendamento para `CANCELADO` (não apaga o registro). Feito pelo cliente (bot) ou pelo dono (web). |
| **Deep link por conta** | `t.me/SeuBot?start=<bot_deep_link_token>`. O `start` identifica qual conta o cliente está agendando. Um único bot da plataforma serve todas as contas (Modelo A). |
| **Calendário (web)** | Visão do dono na área web: dias com marcações e a quantidade de agendamentos por dia. Diferente do **calendário do bot** (seletor de dias/horários livres para o cliente). |

---

## Regras invioláveis do domínio (núcleo Hexagonal)

1. Dois agendamentos **CONFIRMADOS não podem se sobrepor** na mesma conta (uma agenda só).
2. Agendamento só em **horário futuro** e **dentro da disponibilidade** (HorarioAtendimento).
3. A **duração** do agendamento vem do **serviço** escolhido.
4. Cliente, Serviço e Agendamento sempre pertencem à **mesma `conta_id`**.
5. Confirmação é automática → status nasce `CONFIRMADO`; recusa do dono ou cancelamento do
   cliente → `CANCELADO`.

---

## Termos que viram código (módulo / tabela / campo)

| Termo canônico | Vira | Pacote/camada provável |
|---|---|---|
| Conta | tabela `conta` + agregado `Conta` | `domain/conta` |
| HorarioAtendimento | tabela `horario_atendimento` + `HorarioAtendimento` | `domain/horario` |
| Serviço | tabela `servico` + `Servico` | `domain/servico` |
| Cliente | tabela `cliente` + `Cliente` | `domain/cliente` |
| Agendamento | tabela `agendamento` + agregado `Agendamento` | `domain/agendamento` |
| StatusAgendamento | enum próprio (arquivo `.java` separado) | `domain/agendamento` |
| OrigemCliente / OrigemAgendamento | enums próprios (arquivos separados) | `domain/cliente`, `domain/agendamento` |
| DiaSemana | enum próprio | `domain/horario` |
| Slot / Disponibilidade | objetos de valor calculados (não persistidos) | `domain/agendamento` ou serviço de domínio |
