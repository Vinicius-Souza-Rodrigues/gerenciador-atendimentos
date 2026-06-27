package com.plataforma.agendamentos.application.port.in;

import java.time.LocalDateTime;

import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.OrigemAgendamento;

public interface AgendarUseCase {

    Agendamento agendar(AgendarCommand comando);

    record AgendarCommand(
            Long contaId,
            Long telegramUserId,
            String nomeCliente,
            Long servicoId,
            LocalDateTime inicio,
            OrigemAgendamento origem) {
    }
}
