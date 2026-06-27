package com.plataforma.agendamentos.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.plataforma.agendamentos.domain.agendamento.OrigemAgendamento;
import com.plataforma.agendamentos.domain.agendamento.StatusAgendamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamento")
class AgendamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(name = "preco_cobrado", precision = 10, scale = 2)
    private BigDecimal precoCobrado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusAgendamento status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigemAgendamento origem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected AgendamentoEntity() {
    }

    AgendamentoEntity(Long id, Long contaId, Long clienteId, Long servicoId,
                      LocalDateTime inicio, LocalDateTime fim, BigDecimal precoCobrado,
                      StatusAgendamento status, OrigemAgendamento origem,
                      Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.contaId = contaId;
        this.clienteId = clienteId;
        this.servicoId = servicoId;
        this.inicio = inicio;
        this.fim = fim;
        this.precoCobrado = precoCobrado;
        this.status = status;
        this.origem = origem;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    Long getId() { return id; }
    Long getContaId() { return contaId; }
    Long getClienteId() { return clienteId; }
    Long getServicoId() { return servicoId; }
    LocalDateTime getInicio() { return inicio; }
    LocalDateTime getFim() { return fim; }
    BigDecimal getPrecoCobrado() { return precoCobrado; }
    StatusAgendamento getStatus() { return status; }
    OrigemAgendamento getOrigem() { return origem; }
    Instant getCriadoEm() { return criadoEm; }
    Instant getAtualizadoEm() { return atualizadoEm; }
}
