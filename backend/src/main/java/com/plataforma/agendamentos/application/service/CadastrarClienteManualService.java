package com.plataforma.agendamentos.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.CadastrarClienteManualUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.application.port.out.ClienteRepository;
import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.OrigemAgendamento;
import com.plataforma.agendamentos.domain.agendamento.RegrasDeAgendamento;
import com.plataforma.agendamentos.domain.cliente.Cliente;
import com.plataforma.agendamentos.domain.cliente.OrigemCliente;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;
import com.plataforma.agendamentos.domain.servico.Servico;

@Service
@Transactional
class CadastrarClienteManualService implements CadastrarClienteManualUseCase {

    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final HorarioRepository horarioRepository;
    private final RegrasDeAgendamento regras;
    private final Clock clock;

    CadastrarClienteManualService(ClienteRepository clienteRepository,
                                  ServicoRepository servicoRepository,
                                  AgendamentoRepository agendamentoRepository,
                                  HorarioRepository horarioRepository,
                                  RegrasDeAgendamento regras,
                                  Clock clock) {
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.horarioRepository = horarioRepository;
        this.regras = regras;
        this.clock = clock;
    }

    @Override
    public Agendamento cadastrar(CadastrarClienteManualCommand cmd) {
        Servico servico = servicoRepository.buscarPorId(cmd.servicoId())
                .filter(s -> s.contaId().equals(cmd.contaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));

        Cliente cliente = clienteRepository.buscarPorTelefone(cmd.contaId(), cmd.telefone())
                .orElseGet(() -> clienteRepository.salvar(
                        new Cliente(null, cmd.contaId(), cmd.nomeCliente(), null,
                                cmd.telefone(), OrigemCliente.MANUAL, Instant.now(clock))));

        List<HorarioAtendimento> horarios = horarioRepository.listarPorConta(cmd.contaId());
        List<Agendamento> existentes = agendamentoRepository.listarConfirmadosPorConta(cmd.contaId());

        regras.validar(servico, cmd.inicio(), horarios, existentes);

        Agendamento agendamento = Agendamento.confirmar(cmd.contaId(), cliente.id(), servico,
                cmd.inicio(), OrigemAgendamento.MANUAL, Instant.now(clock));

        return agendamentoRepository.salvar(agendamento);
    }
}
