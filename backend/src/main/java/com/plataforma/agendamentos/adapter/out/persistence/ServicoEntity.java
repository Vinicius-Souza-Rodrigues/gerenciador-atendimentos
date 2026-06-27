package com.plataforma.agendamentos.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "servico")
class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Column(nullable = false)
    private String nome;

    @Column(name = "duracao_min", nullable = false)
    private int duracaoMin;

    @Column(length = 1000)
    private String descricao;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ServicoEntity() {
    }

    ServicoEntity(Long id, Long contaId, String nome, int duracaoMin, String descricao,
                  BigDecimal preco, boolean ativo, Instant criadoEm) {
        this.id = id;
        this.contaId = contaId;
        this.nome = nome;
        this.duracaoMin = duracaoMin;
        this.descricao = descricao;
        this.preco = preco;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    Long getId() {
        return id;
    }

    Long getContaId() {
        return contaId;
    }

    String getNome() {
        return nome;
    }

    int getDuracaoMin() {
        return duracaoMin;
    }

    String getDescricao() {
        return descricao;
    }

    BigDecimal getPreco() {
        return preco;
    }

    boolean isAtivo() {
        return ativo;
    }

    Instant getCriadoEm() {
        return criadoEm;
    }
}
