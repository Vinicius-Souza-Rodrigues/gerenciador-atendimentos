package com.plataforma.agendamentos.application.port.out;

import java.util.Optional;

import com.plataforma.agendamentos.domain.conta.Conta;

/** Port de saída para persistir e consultar contas. */
public interface ContaRepository {

    Conta salvar(Conta conta);

    Optional<Conta> buscarPorId(Long id);

    Optional<Conta> buscarPorEmail(String email);

    Optional<Conta> buscarPorBotToken(String botDeepLinkToken);

    boolean existeComEmail(String email);
}
