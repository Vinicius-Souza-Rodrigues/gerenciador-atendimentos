package com.plataforma.agendamentos.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.AgendarUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.ClienteRepository;
import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.RegrasDeAgendamento;
import com.plataforma.agendamentos.domain.cliente.Cliente;
import com.plataforma.agendamentos.domain.cliente.OrigemCliente;

@Service
public class AgendarService implements AgendarUseCase {

    private final ServicoRepository servicoRepository;
    private final HorarioRepository horarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final RegrasDeAgendamento regras;

    public AgendarService(ServicoRepository servicoRepository,
                          HorarioRepository horarioRepository,
                          AgendamentoRepository agendamentoRepository,
                          ClienteRepository clienteRepository,
                          RegrasDeAgendamento regras) {
        this.servicoRepository = servicoRepository;
        this.horarioRepository = horarioRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.regras = regras;
    }

    @Override
    @Transactional
    public Agendamento agendar(AgendarCommand c) {
        var servico = servicoRepository.buscarPorId(c.servicoId())
                .filter(s -> s.contaId().equals(c.contaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));

        var horarios = horarioRepository.listarPorConta(c.contaId());
        var confirmados = agendamentoRepository.listarConfirmadosPorConta(c.contaId());

        regras.validar(servico, c.inicio(), horarios, confirmados);

        var cliente = clienteRepository.buscarPorTelegramUserId(c.contaId(), c.telegramUserId())
                .orElseGet(() -> clienteRepository.salvar(
                        new Cliente(null, c.contaId(), c.nomeCliente(),
                                c.telegramUserId(), null, OrigemCliente.BOT, Instant.now())));

        var agendamento = Agendamento.confirmar(
                c.contaId(), cliente.id(), servico, c.inicio(), c.origem(), Instant.now());

        return agendamentoRepository.salvar(agendamento);
    }
}
