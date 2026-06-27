package com.plataforma.agendamentos.domain.horario;

import java.time.DayOfWeek;

/**
 * Dia da semana em que a conta atende. Mapeia 1:1 para {@link java.time.DayOfWeek},
 * mas é um tipo próprio do domínio (não vaza a API de tempo nas fronteiras).
 */
public enum DiaSemana {
    SEGUNDA,
    TERCA,
    QUARTA,
    QUINTA,
    SEXTA,
    SABADO,
    DOMINGO;

    public static DiaSemana de(DayOfWeek dayOfWeek) {
        return values()[dayOfWeek.getValue() - 1];
    }

    public DayOfWeek paraDayOfWeek() {
        return DayOfWeek.of(ordinal() + 1);
    }
}
