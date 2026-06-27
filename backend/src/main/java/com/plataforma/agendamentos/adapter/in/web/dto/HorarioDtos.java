package com.plataforma.agendamentos.adapter.in.web.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;

import jakarta.validation.constraints.NotNull;

public final class HorarioDtos {

    private HorarioDtos() {
    }

    public record HorarioRequest(
            @NotNull DiaSemana diaSemana,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime horaFim) {
    }

    public record HorarioResponse(
            Long id,
            DiaSemana diaSemana,
            @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
            @JsonFormat(pattern = "HH:mm") LocalTime horaFim) {

        public static HorarioResponse de(HorarioAtendimento h) {
            return new HorarioResponse(h.id(), h.diaSemana(), h.horaInicio(), h.horaFim());
        }
    }
}
