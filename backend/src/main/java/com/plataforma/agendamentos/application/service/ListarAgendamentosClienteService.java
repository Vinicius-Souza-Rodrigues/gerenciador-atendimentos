package com.plataforma.agendamentos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.port.in.ListarAgendamentosClienteUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.ClienteRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;

@Service
public class ListarAgendamentosClienteService implements ListarAgendamentosClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ListarAgendamentosClienteService(ClienteRepository clienteRepository,
                                            AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agendamento> listar(Long contaId, Long telegramUserId) {
        return clienteRepository.buscarPorTelegramUserId(contaId, telegramUserId)
                .map(c -> agendamentoRepository.listarConfirmadosPorCliente(contaId, c.id()))
                .orElse(List.of());
    }
}
