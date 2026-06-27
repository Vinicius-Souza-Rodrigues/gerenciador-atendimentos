package com.plataforma.agendamentos.domain.conta;

import java.time.Instant;
import java.util.Objects;

/**
 * O tenant da plataforma — pessoa ou estabelecimento. Todo dado pertence a uma conta.
 * Domínio puro.
 */
public class Conta {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final String botDeepLinkToken;
    private final Instant criadoEm;

    public Conta(Long id, String nome, String email, String senhaHash,
                 String botDeepLinkToken, Instant criadoEm) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Conta precisa de nome.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Conta precisa de email.");
        }
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Conta precisa de senha.");
        }
        if (botDeepLinkToken == null || botDeepLinkToken.isBlank()) {
            throw new IllegalArgumentException("Conta precisa de token de deep link do bot.");
        }
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.botDeepLinkToken = botDeepLinkToken;
        this.criadoEm = criadoEm;
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String senhaHash() {
        return senhaHash;
    }

    public String botDeepLinkToken() {
        return botDeepLinkToken;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta outra)) return false;
        return id != null && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
