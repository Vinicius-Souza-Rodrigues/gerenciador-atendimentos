package com.plataforma.agendamentos.application.port.in;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.plataforma.agendamentos.domain.agendamento.OrigemAgendamento;
import com.plataforma.agendamentos.domain.agendamento.StatusAgendamento;

public interface VerAgendaUseCase {

    List<ResumoDia> resumoPorDia(Long contaId, LocalDate de, LocalDate ate);

    List<ItemDoDia> listarDoDia(Long contaId, LocalDate data);

    record ResumoDia(LocalDate data, int quantidade) {}

    record ItemDoDia(Long id, LocalDateTime inicio, LocalDateTime fim,
                     String nomeCliente, String nomeServico,
                     StatusAgendamento status, OrigemAgendamento origem) {}
}
