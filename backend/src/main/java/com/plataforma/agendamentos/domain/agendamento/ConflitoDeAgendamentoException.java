package com.plataforma.agendamentos.domain.agendamento;

/** Lançada quando um horário se sobrepõe a outro agendamento CONFIRMADO da mesma conta. */
public class ConflitoDeAgendamentoException extends RuntimeException {
    public ConflitoDeAgendamentoException(String mensagem) {
        super(mensagem);
    }
}
