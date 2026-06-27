package com.plataforma.agendamentos.application.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.RemarcarAgendamentoUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.RegrasDeAgendamento;

@Service
public class RemarcarAgendamentoService implements RemarcarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;
    private final HorarioRepository horarioRepository;
    private final RegrasDeAgendamento regras;

    public RemarcarAgendamentoService(AgendamentoRepository agendamentoRepository,
                                      ServicoRepository servicoRepository,
                                      HorarioRepository horarioRepository,
                                      RegrasDeAgendamento regras) {
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
        this.horarioRepository = horarioRepository;
        this.regras = regras;
    }

    @Override
    @Transactional
    public void remarcar(Long contaId, Long agendamentoId, LocalDateTime novoInicio) {
        var agendamento = agendamentoRepository.buscarPorId(agendamentoId)
                .filter(a -> a.contaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));

        var servico = servicoRepository.buscarPorId(agendamento.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));

        var horarios = horarioRepository.listarPorConta(contaId);

        // Exclui o próprio agendamento da checagem de overlap
        List<Agendamento> confirmados = agendamentoRepository.listarConfirmadosPorConta(contaId)
                .stream()
                .filter(a -> !a.id().equals(agendamentoId))
                .toList();

        regras.validar(servico, novoInicio, horarios, confirmados);
        agendamento.remarcar(novoInicio, servico, Instant.now());
        agendamentoRepository.salvar(agendamento);
    }
}
