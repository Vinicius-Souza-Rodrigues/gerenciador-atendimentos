package com.plataforma.agendamentos.application.port.in;

import com.plataforma.agendamentos.domain.conta.Conta;

/** Obtém a conta autenticada (ex.: para exibir o link do bot). */
public interface ObterContaUseCase {

    Conta obter(Long contaId);
}
