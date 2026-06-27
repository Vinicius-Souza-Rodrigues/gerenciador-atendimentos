package com.plataforma.agendamentos.domain.servico;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

/**
 * Serviço oferecido por uma conta. Define a duração do agendamento (tamanho do slot).
 * Domínio puro: sem anotações de infra.
 */
public class Servico {

    private final Long id;
    private final Long contaId;
    private final String nome;
    private final int duracaoMin;
    private final String descricao;
    private final BigDecimal preco; // opcional (nullable)
    private final boolean ativo;

    public Servico(Long id, Long contaId, String nome, int duracaoMin, String descricao,
                   BigDecimal preco, boolean ativo) {
        if (contaId == null) {
            throw new IllegalArgumentException("Serviço precisa pertencer a uma conta.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Serviço precisa de nome.");
        }
        if (duracaoMin <= 0) {
            throw new IllegalArgumentException("Duração do serviço deve ser maior que zero.");
        }
        if (preco != null && preco.signum() < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo.");
        }
        this.id = id;
        this.contaId = contaId;
        this.nome = nome;
        this.duracaoMin = duracaoMin;
        this.descricao = descricao;
        this.preco = preco;
        this.ativo = ativo;
    }

    public Duration duracao() {
        return Duration.ofMinutes(duracaoMin);
    }

    public Long id() {
        return id;
    }

    public Long contaId() {
        return contaId;
    }

    public String nome() {
        return nome;
    }

    public int duracaoMin() {
        return duracaoMin;
    }

    public String descricao() {
        return descricao;
    }

    public BigDecimal preco() {
        return preco;
    }

    public boolean ativo() {
        return ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico outro)) return false;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
