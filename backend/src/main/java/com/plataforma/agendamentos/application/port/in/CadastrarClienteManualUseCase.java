package com.plataforma.agendamentos.application.port.in;

import java.time.LocalDateTime;

import com.plataforma.agendamentos.domain.agendamento.Agendamento;

public interface CadastrarClienteManualUseCase {

    Agendamento cadastrar(CadastrarClienteManualCommand comando);

    record CadastrarClienteManualCommand(
            Long contaId,
            String nomeCliente,
            String telefone,
            Long servicoId,
            LocalDateTime inicio
    ) {}
}
