package com.plataforma.agendamentos.application.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.CancelarAgendamentoUseCase;
import com.plataforma.agendamentos.application.port.out.AgendamentoRepository;

@Service
public class CancelarAgendamentoService implements CancelarAgendamentoUseCase {

    private final AgendamentoRepository agendamentoRepository;

    public CancelarAgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    @Transactional
    public void cancelar(Long contaId, Long agendamentoId) {
        var agendamento = agendamentoRepository.buscarPorId(agendamentoId)
                .filter(a -> a.contaId().equals(contaId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado."));
        agendamento.cancelar(Instant.now());
        agendamentoRepository.salvar(agendamento);
    }
}
