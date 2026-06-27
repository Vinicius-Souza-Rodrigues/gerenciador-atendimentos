package com.plataforma.agendamentos.domain.agendamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import com.plataforma.agendamentos.domain.servico.Servico;

/**
 * Agendamento de um horário. Carrega as invariantes estruturais do domínio:
 * <ul>
 *   <li>{@code fim = inicio + duração do serviço};</li>
 *   <li>cliente, serviço e agendamento pertencem à mesma conta;</li>
 *   <li>nasce {@link StatusAgendamento#CONFIRMADO} (confirmação automática).</li>
 * </ul>
 *
 * <p>Validações que dependem de contexto externo (horário futuro, dentro da janela,
 * não-sobreposição) ficam em {@link RegrasDeAgendamento}. Domínio puro.
 */
public class Agendamento {

    private final Long id;
    private final Long contaId;
    private final Long clienteId;
    private final Long servicoId;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private final BigDecimal precoCobrado;
    private StatusAgendamento status;
    private final OrigemAgendamento origem;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    /** Construtor completo, usado para reidratar a partir da persistência. */
    public Agendamento(Long id, Long contaId, Long clienteId, Long servicoId,
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

    /**
     * Cria um agendamento já CONFIRMADO. Calcula {@code fim} a partir da duração do serviço
     * e tira o snapshot do preço. Não valida futuro/janela/overlap — isso é
     * {@link RegrasDeAgendamento}.
     */
    public static Agendamento confirmar(Long contaId, Long clienteId, Servico servico,
                                        LocalDateTime inicio, OrigemAgendamento origem,
                                        Instant agora) {
        if (contaId == null || clienteId == null) {
            throw new IllegalArgumentException("Agendamento precisa de conta e cliente.");
        }
        if (servico == null) {
            throw new IllegalArgumentException("Agendamento precisa de serviço.");
        }
        if (inicio == null) {
            throw new IllegalArgumentException("Agendamento precisa de horário de início.");
        }
        if (origem == null) {
            throw new IllegalArgumentException("Agendamento precisa de origem.");
        }
        if (!contaId.equals(servico.contaId())) {
            throw new IllegalArgumentException(
                    "Serviço e agendamento devem pertencer à mesma conta.");
        }
        LocalDateTime fim = inicio.plus(servico.duracao());
        return new Agendamento(null, contaId, clienteId, servico.id(), inicio, fim,
                servico.preco(), StatusAgendamento.CONFIRMADO, origem, agora, agora);
    }

    /** Move o mesmo agendamento para um novo horário (mantém id e status CONFIRMADO). */
    public void remarcar(LocalDateTime novoInicio, Servico servico, Instant agora) {
        if (status != StatusAgendamento.CONFIRMADO) {
            throw new IllegalStateException("Só é possível remarcar um agendamento confirmado.");
        }
        if (servico == null || novoInicio == null) {
            throw new IllegalArgumentException("Remarcar precisa de serviço e novo horário.");
        }
        if (!contaId.equals(servico.contaId())) {
            throw new IllegalArgumentException("Serviço de outra conta.");
        }
        this.inicio = novoInicio;
        this.fim = novoInicio.plus(servico.duracao());
        this.atualizadoEm = agora;
    }

    public void cancelar(Instant agora) {
        this.status = StatusAgendamento.CANCELADO;
        this.atualizadoEm = agora;
    }

    public boolean estaConfirmado() {
        return status == StatusAgendamento.CONFIRMADO;
    }

    public Slot slot() {
        return new Slot(inicio, fim);
    }

    public Long id() {
        return id;
    }

    public Long contaId() {
        return contaId;
    }

    public Long clienteId() {
        return clienteId;
    }

    public Long servicoId() {
        return servicoId;
    }

    public LocalDateTime inicio() {
        return inicio;
    }

    public LocalDateTime fim() {
        return fim;
    }

    public BigDecimal precoCobrado() {
        return precoCobrado;
    }

    public StatusAgendamento status() {
        return status;
    }

    public OrigemAgendamento origem() {
        return origem;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    public Instant atualizadoEm() {
        return atualizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agendamento outro)) return false;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
