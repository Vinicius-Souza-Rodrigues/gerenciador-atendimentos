package com.plataforma.agendamentos.application.port.in;

import java.util.List;

import com.plataforma.agendamentos.domain.agendamento.Agendamento;

public interface ListarAgendamentosClienteUseCase {

    List<Agendamento> listar(Long contaId, Long telegramUserId);
}
