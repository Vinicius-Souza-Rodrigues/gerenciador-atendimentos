package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface ServicoJpaRepository extends JpaRepository<ServicoEntity, Long> {

    List<ServicoEntity> findByContaIdOrderByNomeAsc(Long contaId);
}
