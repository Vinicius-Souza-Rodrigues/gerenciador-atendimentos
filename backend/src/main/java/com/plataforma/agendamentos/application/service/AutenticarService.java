package com.plataforma.agendamentos.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.CredenciaisInvalidasException;
import com.plataforma.agendamentos.application.port.in.AutenticarUseCase;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.application.port.out.SenhaHasher;
import com.plataforma.agendamentos.domain.conta.Conta;

@Service
public class AutenticarService implements AutenticarUseCase {

    private final ContaRepository contaRepository;
    private final SenhaHasher senhaHasher;

    public AutenticarService(ContaRepository contaRepository, SenhaHasher senhaHasher) {
        this.contaRepository = contaRepository;
        this.senhaHasher = senhaHasher;
    }

    @Override
    @Transactional(readOnly = true)
    public Conta autenticar(String email, String senha) {
        String normalizado = email == null ? null : email.trim().toLowerCase();
        Conta conta = contaRepository.buscarPorEmail(normalizado)
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!senhaHasher.confere(senha, conta.senhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return conta;
    }
}
