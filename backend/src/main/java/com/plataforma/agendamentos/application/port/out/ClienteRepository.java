package com.plataforma.agendamentos.application.port.out;

import java.util.Optional;

import com.plataforma.agendamentos.domain.cliente.Cliente;

public interface ClienteRepository {

    Optional<Cliente> buscarPorTelegramUserId(Long contaId, Long telegramUserId);

    Optional<Cliente> buscarPorTelefone(Long contaId, String telefone);

    Optional<Cliente> buscarPorId(Long id);

    Cliente salvar(Cliente cliente);
}
