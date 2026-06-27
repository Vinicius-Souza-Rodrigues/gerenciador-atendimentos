package com.plataforma.agendamentos.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.plataforma.agendamentos.domain.servico.Servico;

/** CRUD de serviços, sempre escopado por conta. */
public interface GerenciarServicoUseCase {

    Servico criar(CriarServicoCommand comando);

    List<Servico> listar(Long contaId);

    Servico atualizar(AtualizarServicoCommand comando);

    void remover(Long contaId, Long servicoId);

    record CriarServicoCommand(Long contaId, String nome, int duracaoMin, String descricao,
                               BigDecimal preco, boolean ativo) {
    }

    record AtualizarServicoCommand(Long contaId, Long servicoId, String nome, int duracaoMin,
                                   String descricao, BigDecimal preco, boolean ativo) {
    }
}
