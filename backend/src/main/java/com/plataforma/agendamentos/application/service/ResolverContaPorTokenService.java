package com.plataforma.agendamentos.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.ResolverContaPorTokenUseCase;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.domain.conta.Conta;

@Service
public class ResolverContaPorTokenService implements ResolverContaPorTokenUseCase {

    private final ContaRepository contaRepository;

    public ResolverContaPorTokenService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Conta resolver(String botDeepLinkToken) {
        return contaRepository.buscarPorBotToken(botDeepLinkToken)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada para o token."));
    }
}
