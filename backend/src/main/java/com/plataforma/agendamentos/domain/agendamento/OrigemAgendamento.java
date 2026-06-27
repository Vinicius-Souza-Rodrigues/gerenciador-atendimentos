package com.plataforma.agendamentos.domain.agendamento;

/** De onde veio o agendamento: pelo bot de Telegram ou pelo cadastro manual da web. */
public enum OrigemAgendamento {
    BOT,
    MANUAL
}
