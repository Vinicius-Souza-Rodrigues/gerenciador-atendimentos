package com.plataforma.agendamentos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.ConsultarDisponibilidadeUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.agendamento.CalculadoraDisponibilidade;
import com.plataforma.agendamentos.domain.agendamento.Slot;

@Service
public class ConsultarDisponibilidadeService implements ConsultarDisponibilidadeUseCase {

    private final ServicoRepository servicoRepository;
    private final HorarioRepository horarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final CalculadoraDisponibilidade calculadora;

    public ConsultarDisponibilidadeService(ServicoRepository servicoRepository,
                                           HorarioRepository horarioRepository,
                                           AgendamentoRepository agendamentoRepository,
                                           CalculadoraDisponibilidade calculadora) {
        this.servicoRepository = servicoRepository;
        this.horarioRepository = horarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.calculadora = calculadora;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slot> consultar(ConsultarDisponibilidadeCommand c) {
        var servico = servicoRepository.buscarPorId(c.servicoId())
                .filter(s -> s.contaId().equals(c.contaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));
        var horarios = horarioRepository.listarPorConta(c.contaId());
        var confirmados = agendamentoRepository.listarConfirmadosPorConta(c.contaId());
        return calculadora.slotsLivres(servico, horarios, confirmados, c.de(), c.ate());
    }
}
