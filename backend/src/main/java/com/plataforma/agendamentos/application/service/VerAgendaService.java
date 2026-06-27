package com.plataforma.agendamentos.application.service;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.port.in.VerAgendaUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.ClienteRepository;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.cliente.Cliente;
import com.plataforma.agendamentos.domain.servico.Servico;

@Service
@Transactional(readOnly = true)
class VerAgendaService implements VerAgendaUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;

    VerAgendaService(AgendamentoRepository agendamentoRepository,
                     ClienteRepository clienteRepository,
                     ServicoRepository servicoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
    }

    @Override
    public List<ResumoDia> resumoPorDia(Long contaId, LocalDate de, LocalDate ate) {
        return agendamentoRepository.listarConfirmadosPorContaEPeriodo(contaId, de, ate)
                .stream()
                .collect(groupingBy(a -> a.inicio().toLocalDate(), counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ResumoDia(e.getKey(), e.getValue().intValue()))
                .toList();
    }

    @Override
    public List<ItemDoDia> listarDoDia(Long contaId, LocalDate data) {
        List<Agendamento> agendamentos =
                agendamentoRepository.listarConfirmadosPorContaEPeriodo(contaId, data, data);

        Map<Long, String> nomesServico = servicoRepository.listarPorConta(contaId)
                .stream().collect(toMap(Servico::id, Servico::nome));

        return agendamentos.stream()
                .sorted(Comparator.comparing(Agendamento::inicio))
                .map(a -> {
                    String nomeCliente = clienteRepository.buscarPorId(a.clienteId())
                            .map(Cliente::nome).orElse("Cliente #" + a.clienteId());
                    String nomeServico = nomesServico.getOrDefault(a.servicoId(),
                            "Serviço #" + a.servicoId());
                    return new ItemDoDia(a.id(), a.inicio(), a.fim(),
                            nomeCliente, nomeServico, a.status(), a.origem());
                })
                .toList();
    }
}
