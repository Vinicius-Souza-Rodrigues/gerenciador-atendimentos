package com.plataforma.agendamentos.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.plataforma.agendamentos.domain.agendamento.Agendamento;

public interface AgendamentoRepository {

    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(Long id);

    List<Agendamento> listarConfirmadosPorConta(Long contaId);

    List<Agendamento> listarConfirmadosPorCliente(Long contaId, Long clienteId);

    List<Agendamento> listarConfirmadosPorContaEPeriodo(Long contaId, LocalDate de, LocalDate ate);
}
