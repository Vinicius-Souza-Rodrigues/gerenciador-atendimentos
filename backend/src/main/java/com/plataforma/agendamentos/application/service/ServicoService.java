package com.plataforma.agendamentos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.servico.Servico;

@Service
public class ServicoService implements GerenciarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    @Transactional
    public Servico criar(CriarServicoCommand c) {
        Servico servico = new Servico(null, c.contaId(), c.nome(), c.duracaoMin(),
                c.descricao(), c.preco(), c.ativo());
        return servicoRepository.salvar(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Servico> listar(Long contaId) {
        return servicoRepository.listarPorConta(contaId);
    }

    @Override
    @Transactional
    public Servico atualizar(AtualizarServicoCommand c) {
        Servico existente = buscarDaConta(c.servicoId(), c.contaId());
        Servico atualizado = new Servico(existente.id(), c.contaId(), c.nome(), c.duracaoMin(),
                c.descricao(), c.preco(), c.ativo());
        return servicoRepository.salvar(atualizado);
    }

    @Override
    @Transactional
    public void remover(Long contaId, Long servicoId) {
        Servico existente = buscarDaConta(servicoId, contaId);
        servicoRepository.remover(existente.id());
    }

    private Servico buscarDaConta(Long servicoId, Long contaId) {
        Servico servico = servicoRepository.buscarPorId(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado."));
        if (!servico.contaId().equals(contaId)) {
            // Não revela que existe em outra conta — trata como inexistente.
            throw new RecursoNaoEncontradoException("Serviço não encontrado.");
        }
        return servico;
    }
}
