package com.plataforma.agendamentos.application.exception;

/** Tentativa de criar conta com email já existente. */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException(String email) {
        super("Já existe uma conta com o email: " + email);
    }
}
