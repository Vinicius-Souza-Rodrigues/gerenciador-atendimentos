package com.plataforma.agendamentos.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.plataforma.agendamentos.domain.agendamento.Slot;

public interface ConsultarDisponibilidadeUseCase {

    List<Slot> consultar(ConsultarDisponibilidadeCommand comando);

    record ConsultarDisponibilidadeCommand(Long contaId, Long servicoId, LocalDate de, LocalDate ate) {
    }
}
