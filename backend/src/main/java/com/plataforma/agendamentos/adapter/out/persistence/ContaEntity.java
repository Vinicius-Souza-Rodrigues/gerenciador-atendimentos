package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conta")
class ContaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "bot_deep_link_token", nullable = false, unique = true)
    private String botDeepLinkToken;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ContaEntity() {
    }

    ContaEntity(Long id, String nome, String email, String senhaHash,
                String botDeepLinkToken, Instant criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.botDeepLinkToken = botDeepLinkToken;
        this.criadoEm = criadoEm;
    }

    Long getId() {
        return id;
    }

    String getNome() {
        return nome;
    }

    String getEmail() {
        return email;
    }

    String getSenhaHash() {
        return senhaHash;
    }

    String getBotDeepLinkToken() {
        return botDeepLinkToken;
    }

    Instant getCriadoEm() {
        return criadoEm;
    }
}
