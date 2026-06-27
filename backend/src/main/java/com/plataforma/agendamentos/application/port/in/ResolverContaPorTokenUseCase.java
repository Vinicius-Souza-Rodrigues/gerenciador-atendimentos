package com.plataforma.agendamentos.application.port.in;

import com.plataforma.agendamentos.domain.conta.Conta;

/** Resolve a conta a partir do token do deep link (`t.me/SeuBot?start=&lt;token&gt;`). */
public interface ResolverContaPorTokenUseCase {

    Conta resolver(String botDeepLinkToken);
}
