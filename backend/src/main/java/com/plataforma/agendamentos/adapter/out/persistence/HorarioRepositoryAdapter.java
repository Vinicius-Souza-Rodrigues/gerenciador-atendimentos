package com.plataforma.agendamentos.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;

@Repository
class HorarioRepositoryAdapter implements HorarioRepository {

    private final HorarioJpaRepository jpa;

    HorarioRepositoryAdapter(HorarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<HorarioAtendimento> listarPorConta(Long contaId) {
        return jpa.findByContaId(contaId).stream()
                .map(HorarioRepositoryAdapter::paraDomain)
                .toList();
    }

    @Override
    public List<HorarioAtendimento> substituirDaConta(Long contaId, List<HorarioAtendimento> horarios) {
        jpa.deleteByContaId(contaId);
        List<HorarioAtendimentoEntity> entidades = horarios.stream()
                .map(h -> paraEntity(contaId, h))
                .toList();
        return jpa.saveAll(entidades).stream()
                .map(HorarioRepositoryAdapter::paraDomain)
                .toList();
    }

    private static HorarioAtendimentoEntity paraEntity(Long contaId, HorarioAtendimento h) {
        return new HorarioAtendimentoEntity(h.id(), contaId, h.diaSemana(),
                h.horaInicio(), h.horaFim());
    }

    private static HorarioAtendimento paraDomain(HorarioAtendimentoEntity e) {
        return new HorarioAtendimento(e.getId(), e.getContaId(), e.getDiaSemana(),
                e.getHoraInicio(), e.getHoraFim());
    }
}
