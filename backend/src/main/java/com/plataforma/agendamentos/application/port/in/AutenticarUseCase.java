package com.plataforma.agendamentos.application.port.in;

import com.plataforma.agendamentos.domain.conta.Conta;

/**
 * Autentica por email + senha e devolve a conta. A geração do JWT é responsabilidade do
 * adapter web (infra), não da aplicação.
 */
public interface AutenticarUseCase {

    Conta autenticar(String email, String senha);
}
