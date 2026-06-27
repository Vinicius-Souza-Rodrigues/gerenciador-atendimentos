package com.plataforma.agendamentos.adapter.in.web.dto;

import java.math.BigDecimal;

import com.plataforma.agendamentos.domain.servico.Servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public final class ServicoDtos {

    private ServicoDtos() {
    }

    public record ServicoRequest(
            @NotBlank String nome,
            @Positive int duracaoMin,
            String descricao,
            @PositiveOrZero BigDecimal preco,
            Boolean ativo) {

        public boolean ativoOuPadrao() {
            return ativo == null || ativo;
        }
    }

    public record ServicoResponse(
            Long id, Long contaId, String nome, int duracaoMin, String descricao,
            BigDecimal preco, boolean ativo) {

        public static ServicoResponse de(Servico s) {
            return new ServicoResponse(s.id(), s.contaId(), s.nome(), s.duracaoMin(),
                    s.descricao(), s.preco(), s.ativo());
        }
    }
}
