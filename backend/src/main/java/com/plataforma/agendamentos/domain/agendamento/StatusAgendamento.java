package com.plataforma.agendamentos.domain.agendamento;

/**
 * Estado de um agendamento.
 *
 * <p>Nasce {@link #CONFIRMADO} (confirmação automática). Recusa do dono ou cancelamento do
 * cliente → {@link #CANCELADO}. {@link #CONCLUIDO} fica reservado para pós-MVP.
 */
public enum StatusAgendamento {
    CONFIRMADO,
    CANCELADO,
    CONCLUIDO
}
