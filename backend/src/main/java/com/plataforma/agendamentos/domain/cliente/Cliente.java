package com.plataforma.agendamentos.domain.cliente;

import java.time.Instant;
import java.util.Objects;

/**
 * Cliente de uma conta. Vem do bot (identificado por {@code telegramUserId}) ou do cadastro
 * manual (identificado por {@code telefone}). Regra: tem pelo menos um dos dois. Domínio puro.
 */
public class Cliente {

    private final Long id;
    private final Long contaId;
    private final String nome;
    private final Long telegramUserId; // nullable
    private final String telefone;     // nullable
    private final OrigemCliente origem;
    private final Instant criadoEm;

    public Cliente(Long id, Long contaId, String nome, Long telegramUserId, String telefone,
                   OrigemCliente origem, Instant criadoEm) {
        if (contaId == null) {
            throw new IllegalArgumentException("Cliente precisa pertencer a uma conta.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Cliente precisa de nome.");
        }
        if (origem == null) {
            throw new IllegalArgumentException("Cliente precisa de origem.");
        }
        boolean temTelegram = telegramUserId != null;
        boolean temTelefone = telefone != null && !telefone.isBlank();
        if (!temTelegram && !temTelefone) {
            throw new IllegalArgumentException(
                    "Cliente precisa de telegramUserId OU telefone (ao menos um).");
        }
        this.id = id;
        this.contaId = contaId;
        this.nome = nome;
        this.telegramUserId = telegramUserId;
        this.telefone = telefone;
        this.origem = origem;
        this.criadoEm = criadoEm;
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

    public Long telegramUserId() {
        return telegramUserId;
    }

    public String telefone() {
        return telefone;
    }

    public OrigemCliente origem() {
        return origem;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cliente outro)) return false;
        return id != null && id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
