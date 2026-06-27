package com.plataforma.agendamentos.application.exception;

/** Recurso inexistente ou de outra conta (não revela qual dos dois). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
