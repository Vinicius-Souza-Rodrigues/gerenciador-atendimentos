package com.plataforma.agendamentos.domain.horario;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Uma janela de atendimento da conta num dia da semana (ex.: SEGUNDA 09:00–12:00).
 * Pode haver mais de uma janela por dia. Domínio puro.
 */
public class HorarioAtendimento {

    private final Long id;
    private final Long contaId;
    private final DiaSemana diaSemana;
    private final LocalTime horaInicio;
    private final LocalTime horaFim;

    public HorarioAtendimento(Long id, Long contaId, DiaSemana diaSemana,
                              LocalTime horaInicio, LocalTime horaFim) {
        if (contaId == null) {
            throw new IllegalArgumentException("Horário precisa pertencer a uma conta.");
        }
        if (diaSemana == null) {
            throw new IllegalArgumentException("Horário precisa de dia da semana.");
        }
        if (horaInicio == null || horaFim == null) {
            throw new IllegalArgumentException("Horário precisa de início e fim.");
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new IllegalArgumentException("Fim do horário deve ser depois do início.");
        }
        this.id = id;
        this.contaId = contaId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public Long id() {
        return id;
    }

    public Long contaId() {
        return contaId;
    }

    public DiaSemana diaSemana() {
        return diaSemana;
    }

    public LocalTime horaInicio() {
        return horaInicio;
    }

    public LocalTime horaFim() {
        return horaFim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HorarioAtendimento outro)) return false;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
