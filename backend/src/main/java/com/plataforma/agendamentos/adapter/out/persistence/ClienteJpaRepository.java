package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByContaIdAndTelegramUserId(Long contaId, Long telegramUserId);

    Optional<ClienteEntity> findByContaIdAndTelefone(Long contaId, String telefone);
}
