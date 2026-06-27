package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ContaJpaRepository extends JpaRepository<ContaEntity, Long> {

    Optional<ContaEntity> findByEmail(String email);

    Optional<ContaEntity> findByBotDeepLinkToken(String botDeepLinkToken);

    boolean existsByEmail(String email);
}
