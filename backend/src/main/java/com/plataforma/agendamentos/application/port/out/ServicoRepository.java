package com.plataforma.agendamentos.application.port.out;

import java.util.List;
import java.util.Optional;

import com.plataforma.agendamentos.domain.servico.Servico;

/** Port de saída para persistir e consultar serviços. */
public interface ServicoRepository {

    Servico salvar(Servico servico);

    Optional<Servico> buscarPorId(Long id);

    List<Servico> listarPorConta(Long contaId);

    void remover(Long id);
}
