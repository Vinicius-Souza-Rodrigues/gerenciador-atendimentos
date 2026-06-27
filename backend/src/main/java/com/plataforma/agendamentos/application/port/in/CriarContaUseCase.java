package com.plataforma.agendamentos.application.port.in;

import com.plataforma.agendamentos.domain.conta.Conta;

/** Criação de conta (signup self-service). */
public interface CriarContaUseCase {

    Conta criar(CriarContaCommand comando);

    record CriarContaCommand(String nome, String email, String senha) {
    }
}
