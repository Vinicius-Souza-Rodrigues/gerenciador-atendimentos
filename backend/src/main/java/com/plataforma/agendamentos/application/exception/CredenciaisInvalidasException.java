package com.plataforma.agendamentos.application.exception;

/** Email ou senha inválidos no login. */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Email ou senha inválidos.");
    }
}
