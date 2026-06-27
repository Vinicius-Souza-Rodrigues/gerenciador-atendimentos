package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.Instant;

import com.plataforma.agendamentos.domain.cliente.OrigemCliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(nullable = false)
    private String nome;

    @Column(name = "telegram_user_id")
    private Long telegramUserId;

    @Column(length = 30)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrigemCliente origem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected ClienteEntity() {
    }

    ClienteEntity(Long id, Long contaId, String nome, Long telegramUserId, String telefone,
                  OrigemCliente origem, Instant criadoEm) {
        this.id = id;
        this.contaId = contaId;
        this.nome = nome;
        this.telegramUserId = telegramUserId;
        this.telefone = telefone;
        this.origem = origem;
        this.criadoEm = criadoEm;
    }

    Long getId() { return id; }
    Long getContaId() { return contaId; }
    String getNome() { return nome; }
    Long getTelegramUserId() { return telegramUserId; }
    String getTelefone() { return telefone; }
    OrigemCliente getOrigem() { return origem; }
    Instant getCriadoEm() { return criadoEm; }
}
