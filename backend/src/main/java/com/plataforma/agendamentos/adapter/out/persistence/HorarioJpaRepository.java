package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface HorarioJpaRepository extends JpaRepository<HorarioAtendimentoEntity, Long> {

    List<HorarioAtendimentoEntity> findByContaId(Long contaId);

    void deleteByContaId(Long contaId);
}
