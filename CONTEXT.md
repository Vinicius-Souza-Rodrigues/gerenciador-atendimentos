# CONTEXT — Glossário de Domínio (gerenciador-atendimentos)

**Data:** 2026-06-24
**Origem:** BRIEFING + Domain Grill (Etapa 1.5)

Glossário canônico. Estes são os termos que viram módulo, tabela ou campo. Devem ser usados
sem sinônimos no SDD e no código.

---

## Atores

| Termo | Definição precisa | Vira código? |
|---|---|---|
| **Conta** | O *tenant*. Pessoa ou estabelecimento que se cadastra, faz login na web e é dona de serviços, clientes, horários e agendamentos. Isolamento multi-tenant por coluna `conta_id`. | Tabela `conta` |
| **Dono da conta** | A pessoa física que opera a Conta pela área web. No MVP, 1 login por Conta (sem múltiplos usuários por conta). | — (atributos da Conta) |
| **Cliente** | Quem agenda. Vem de 2 origens: `BOT` (identificado pelo `telegram_user_id`) ou `MANUAL` (cadastrado na web com `telefone`). Regra: todo Cliente tem `telegram_user_id` **ou** `telefone` (≥1). | Tabela `cliente` |
| **Cliente final** | Sinônimo de Cliente na ótica do produto (quem usa o bot). Mesmo conceito de **Cliente**. | — |

---

## Conceitos centrais

| Termo | Definição precisa | Vira código? |
|---|---|---|
| **Serviço** | Item agendável criado pelo dono: `nome`, `duracao_min`, `descricao`, `preco` (opcional/nullable), `ativo`. Todo Agendamento referencia um Serviço já criado. A duração do Serviço define quantos slots o Agendamento ocupa. | Tabela `servico` |
| **HorarioAtendimento** | Janela recorrente em que a Conta atende: `dia_semana` (SEG..DOM) + `hora_inicio`/`hora_fim`. Uma Conta pode ter várias janelas (vários dias / vários blocos por dia). | Tabela `horario_atendimento` |
| **Agendamento** | Reserva de um horário por um Cliente para um Serviço. Campos: `conta_id`, `cliente_id`, `servico_id`, `inicio`, `fim` (= início + duração do serviço), `preco_cobrado` (snapshot opcional), `status`, `origem`, timestamps. | Tabela `agendamento` |
| **Disponibilidade** | Conjunto de horários oferecíveis ao cliente. Calculada como: a **grade de slots** dentro dos HorarioAtendimento da Conta, **menos** os slots já ocupados por Agendamentos `CONFIRMADO`, considerando que o Serviço escolhido ocupa N slots consecutivos. Só horários **futuros**. | Lógica de domínio |
| **Duração do serviço** vs **Granularidade da grade** | **Dois conceitos distintos.** A **duração** vem do Serviço (`servico.duracao_min`) e é **variável** (corte 30min, coloração 90min) — define quanto tempo o Agendamento ocupa. A **granularidade** (`conta.granularidade_min`) define apenas **o espaçamento dos horários de INÍCIO** oferecidos. Um serviço de 90min numa grade de 30min ocupa 3 fatias consecutivas (09:00→10:30). | `servico.duracao_min` + `conta.granularidade_min` |
| **Slot** | Unidade fixa da grade de horários de início. Tamanho = `granularidade_min` da Conta (**30 min** no MVP). Um Serviço de duração D ocupa `ceil(D / granularidade_min)` slots consecutivos a partir de um ponto da grade. | Lógica de domínio + campo `conta.granularidade_min` |
| **Deep link token** | O `start=<token>` de `t.me/SeuBot?start=<token>`. Identifica qual Conta o cliente está acessando pelo bot (Modelo A: 1 bot, N contas). Campo `bot_deep_link_token` na Conta. | Campo `conta.bot_deep_link_token` |

---

## Enums (cada um em arquivo próprio — regra do briefing)

| Enum | Valores | Significado |
|---|---|---|
| **StatusAgendamento** | `CONFIRMADO`, `CANCELADO`, `CONCLUIDO` | Nasce `CONFIRMADO` (auto-confirmação). Recusa do dono ou cancelamento do cliente → `CANCELADO`. `CONCLUIDO` = atendimento já ocorreu. |
| **OrigemCliente** | `BOT`, `MANUAL` | De onde o Cliente veio. |
| **OrigemAgendamento** | `BOT`, `MANUAL` | Se o Agendamento nasceu pelo bot ou por cadastro manual na web. |
| **DiaSemana** | `SEG`..`DOM` | Dia da janela de HorarioAtendimento. |

---

## Regras invioláveis do domínio (núcleo Hexagonal)

1. **Sem sobreposição entre CONFIRMADOS** na mesma Conta. Intervalos são **meio-abertos `[inicio, fim)`**: 09:00–09:30 e 09:30–10:00 **não** conflitam. Agendamentos `CANCELADO` e `CONCLUIDO` **liberam** o horário (não bloqueiam a grade).
2. Agendamento só em **horário futuro** e **dentro da disponibilidade** (HorarioAtendimento) da Conta.
3. A **duração** do Agendamento vem do **Serviço** escolhido (define quantos slots ocupa).
4. Cliente, Serviço e Agendamento sempre pertencem à **mesma `conta_id`**.
5. Confirmação é automática → status nasce `CONFIRMADO`. Recusa do dono (web) ou cancelamento do cliente (bot) → `CANCELADO`.

---

## Decisões do Domain Grill (2026-06-24)

| Tema | Decisão | Consequência no código |
|---|---|---|
| **Geração de horários** | **Grade fixa de slots** (confirmado em 2026-06-24, incl. ressalva do usuário sobre duração variável). Horários de início sempre na grade; serviços longos ocupam várias fatias. Aceita-se "tempo morto" para durações quebradas. `granularidade_min` por Conta, **30 min** no MVP (coluna existe; UI para alterar é pós-MVP). | Campo `conta.granularidade_min` (default 30); disponibilidade calculada sobre a grade. |
| **Fuso horário** | **Fixo `America/Sao_Paulo`** no MVP. Armazenar em **UTC** no banco, converter na borda (adapters bot/web). | Sem campo de fuso por conta no MVP. |
| **Remarcar** | **Atualiza o mesmo registro** de Agendamento (mantém `id`, muda `inicio`/`fim`). Não preserva o horário antigo. | Operação de update; sem novo registro. `atualizado_em` reflete a mudança. |
| **Sobreposição** | Só **CONFIRMADO** bloqueia; intervalo **`[inicio, fim)`** meio-aberto. | Query de conflito filtra `status = CONFIRMADO` e usa comparação meio-aberta. |

---

## Pontos ainda em aberto (a confirmar antes de virar código)

- (nenhum no domínio — slots, fuso, remarcar e sobreposição estão resolvidos; stack está no ADR.)
