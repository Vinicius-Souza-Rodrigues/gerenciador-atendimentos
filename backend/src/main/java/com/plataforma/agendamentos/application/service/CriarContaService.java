package com.plataforma.agendamentos.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.EmailJaCadastradoException;
import com.plataforma.agendamentos.application.port.in.CriarContaUseCase;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.application.port.out.SenhaHasher;
import com.plataforma.agendamentos.domain.conta.Conta;

@Service
public class CriarContaService implements CriarContaUseCase {

    private final ContaRepository contaRepository;
    private final SenhaHasher senhaHasher;

    public CriarContaService(ContaRepository contaRepository, SenhaHasher senhaHasher) {
        this.contaRepository = contaRepository;
        this.senhaHasher = senhaHasher;
    }

    @Override
    @Transactional
    public Conta criar(CriarContaCommand comando) {
        String email = comando.email() == null ? null : comando.email().trim().toLowerCase();
        if (email != null && contaRepository.existeComEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
        String senhaHash = senhaHasher.hash(comando.senha());
        String token = gerarToken();
        Conta conta = new Conta(null, comando.nome(), email, senhaHash, token, Instant.now());
        return contaRepository.salvar(conta);
    }

    private String gerarToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
