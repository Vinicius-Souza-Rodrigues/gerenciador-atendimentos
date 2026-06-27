package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.LocalTime;

import com.plataforma.agendamentos.domain.horario.DiaSemana;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "horario_atendimento")
class HorarioAtendimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private DiaSemana diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    protected HorarioAtendimentoEntity() {
    }

    HorarioAtendimentoEntity(Long id, Long contaId, DiaSemana diaSemana,
                             LocalTime horaInicio, LocalTime horaFim) {
        this.id = id;
        this.contaId = contaId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    Long getId() {
        return id;
    }

    Long getContaId() {
        return contaId;
    }

    DiaSemana getDiaSemana() {
        return diaSemana;
    }

    LocalTime getHoraInicio() {
        return horaInicio;
    }

    LocalTime getHoraFim() {
        return horaFim;
    }
}
