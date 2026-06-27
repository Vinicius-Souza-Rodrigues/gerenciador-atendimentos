package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.plataforma.agendamentos.application.port.out.ClienteRepository;
import com.plataforma.agendamentos.domain.cliente.Cliente;

@Repository
class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpa;

    ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Cliente> buscarPorTelegramUserId(Long contaId, Long telegramUserId) {
        return jpa.findByContaIdAndTelegramUserId(contaId, telegramUserId)
                .map(ClienteRepositoryAdapter::paraDomain);
    }

    @Override
    public Optional<Cliente> buscarPorTelefone(Long contaId, String telefone) {
        return jpa.findByContaIdAndTelefone(contaId, telefone)
                .map(ClienteRepositoryAdapter::paraDomain);
    }

    @Override
    public Optional<Cliente> buscarPorId(Long id) {
        return jpa.findById(id).map(ClienteRepositoryAdapter::paraDomain);
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        return paraDomain(jpa.save(paraEntity(cliente)));
    }

    private static ClienteEntity paraEntity(Cliente c) {
        return new ClienteEntity(c.id(), c.contaId(), c.nome(),
                c.telegramUserId(), c.telefone(), c.origem(), c.criadoEm());
    }

    private static Cliente paraDomain(ClienteEntity e) {
        return new Cliente(e.getId(), e.getContaId(), e.getNome(),
                e.getTelegramUserId(), e.getTelefone(), e.getOrigem(), e.getCriadoEm());
    }
}
