package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.domain.conta.Conta;

@Repository
class ContaRepositoryAdapter implements ContaRepository {

    private final ContaJpaRepository jpa;

    ContaRepositoryAdapter(ContaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Conta salvar(Conta conta) {
        ContaEntity salvo = jpa.save(paraEntity(conta));
        return paraDomain(salvo);
    }

    @Override
    public Optional<Conta> buscarPorId(Long id) {
        return jpa.findById(id).map(ContaRepositoryAdapter::paraDomain);
    }

    @Override
    public Optional<Conta> buscarPorEmail(String email) {
        return jpa.findByEmail(email).map(ContaRepositoryAdapter::paraDomain);
    }

    @Override
    public Optional<Conta> buscarPorBotToken(String botDeepLinkToken) {
        return jpa.findByBotDeepLinkToken(botDeepLinkToken).map(ContaRepositoryAdapter::paraDomain);
    }

    @Override
    public boolean existeComEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private static ContaEntity paraEntity(Conta c) {
        return new ContaEntity(c.id(), c.nome(), c.email(), c.senhaHash(),
                c.botDeepLinkToken(), c.criadoEm());
    }

    private static Conta paraDomain(ContaEntity e) {
        return new Conta(e.getId(), e.getNome(), e.getEmail(), e.getSenhaHash(),
                e.getBotDeepLinkToken(), e.getCriadoEm());
    }
}
