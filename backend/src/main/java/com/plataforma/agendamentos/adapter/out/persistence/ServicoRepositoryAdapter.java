package com.plataforma.agendamentos.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.servico.Servico;

@Repository
class ServicoRepositoryAdapter implements ServicoRepository {

    private final ServicoJpaRepository jpa;

    ServicoRepositoryAdapter(ServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Servico salvar(Servico servico) {
        Instant criadoEm = servico.id() == null
                ? Instant.now()
                : jpa.findById(servico.id()).map(ServicoEntity::getCriadoEm).orElse(Instant.now());
        ServicoEntity salvo = jpa.save(paraEntity(servico, criadoEm));
        return paraDomain(salvo);
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        return jpa.findById(id).map(ServicoRepositoryAdapter::paraDomain);
    }

    @Override
    public List<Servico> listarPorConta(Long contaId) {
        return jpa.findByContaIdOrderByNomeAsc(contaId).stream()
                .map(ServicoRepositoryAdapter::paraDomain)
                .toList();
    }

    @Override
    public void remover(Long id) {
        jpa.deleteById(id);
    }

    private static ServicoEntity paraEntity(Servico s, Instant criadoEm) {
        return new ServicoEntity(s.id(), s.contaId(), s.nome(), s.duracaoMin(),
                s.descricao(), s.preco(), s.ativo(), criadoEm);
    }

    private static Servico paraDomain(ServicoEntity e) {
        return new Servico(e.getId(), e.getContaId(), e.getNome(), e.getDuracaoMin(),
                e.getDescricao(), e.getPreco(), e.isAtivo());
    }
}
