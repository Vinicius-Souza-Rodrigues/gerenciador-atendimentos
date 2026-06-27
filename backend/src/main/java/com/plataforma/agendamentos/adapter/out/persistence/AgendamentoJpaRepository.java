package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plataforma.agendamentos.domain.agendamento.StatusAgendamento;

interface AgendamentoJpaRepository extends JpaRepository<AgendamentoEntity, Long> {

    List<AgendamentoEntity> findByContaIdAndStatus(Long contaId, StatusAgendamento status);

    List<AgendamentoEntity> findByContaIdAndClienteIdAndStatus(Long contaId, Long clienteId,
                                                                StatusAgendamento status);

    List<AgendamentoEntity> findByContaIdAndStatusAndInicioGreaterThanEqualAndInicioLessThan(
            Long contaId, StatusAgendamento status, LocalDateTime inicioMin, LocalDateTime inicioMax);
}
