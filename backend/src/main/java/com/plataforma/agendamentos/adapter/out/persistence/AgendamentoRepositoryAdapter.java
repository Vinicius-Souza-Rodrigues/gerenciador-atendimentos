package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;
import com.plataforma.agendamentos.domain.agendamento.StatusAgendamento;

@Repository
class AgendamentoRepositoryAdapter implements AgendamentoRepository {

    private final AgendamentoJpaRepository jpa;

    AgendamentoRepositoryAdapter(AgendamentoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        return paraDomain(jpa.save(paraEntity(agendamento)));
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        return jpa.findById(id).map(AgendamentoRepositoryAdapter::paraDomain);
    }

    @Override
    public List<Agendamento> listarConfirmadosPorConta(Long contaId) {
        return jpa.findByContaIdAndStatus(contaId, StatusAgendamento.CONFIRMADO)
                .stream().map(AgendamentoRepositoryAdapter::paraDomain).toList();
    }

    @Override
    public List<Agendamento> listarConfirmadosPorCliente(Long contaId, Long clienteId) {
        return jpa.findByContaIdAndClienteIdAndStatus(contaId, clienteId, StatusAgendamento.CONFIRMADO)
                .stream().map(AgendamentoRepositoryAdapter::paraDomain).toList();
    }

    @Override
    public List<Agendamento> listarConfirmadosPorContaEPeriodo(Long contaId, LocalDate de, LocalDate ate) {
        return jpa.findByContaIdAndStatusAndInicioGreaterThanEqualAndInicioLessThan(
                        contaId, StatusAgendamento.CONFIRMADO,
                        de.atStartOfDay(), ate.plusDays(1).atStartOfDay())
                .stream().map(AgendamentoRepositoryAdapter::paraDomain).toList();
    }

    private static AgendamentoEntity paraEntity(Agendamento a) {
        return new AgendamentoEntity(a.id(), a.contaId(), a.clienteId(), a.servicoId(),
                a.inicio(), a.fim(), a.precoCobrado(), a.status(), a.origem(),
                a.criadoEm(), a.atualizadoEm());
    }

    private static Agendamento paraDomain(AgendamentoEntity e) {
        return new Agendamento(e.getId(), e.getContaId(), e.getClienteId(), e.getServicoId(),
                e.getInicio(), e.getFim(), e.getPrecoCobrado(), e.getStatus(), e.getOrigem(),
                e.getCriadoEm(), e.getAtualizadoEm());
    }
}
