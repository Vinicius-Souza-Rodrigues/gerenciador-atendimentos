package com.plataforma.agendamentos.domain.agendamento;

/**
 * Lançada quando o horário pedido é inválido por contexto: está no passado ou fora de
 * qualquer janela de atendimento (HorarioAtendimento) da conta.
 */
public class HorarioIndisponivelException extends RuntimeException {
    public HorarioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
