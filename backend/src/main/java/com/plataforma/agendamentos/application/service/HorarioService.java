package com.plataforma.agendamentos.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.port.in.GerenciarHorarioUseCase;
import com.plataforma.agendamentos.application.port.out.HorarioRepository;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;

@Service
public class HorarioService implements GerenciarHorarioUseCase {

    private final HorarioRepository horarioRepository;

    public HorarioService(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioAtendimento> listar(Long contaId) {
        return horarioRepository.listarPorConta(contaId);
    }

    @Override
    @Transactional
    public List<HorarioAtendimento> definir(Long contaId, List<JanelaCommand> janelas) {
        List<HorarioAtendimento> horarios = janelas.stream()
                .map(j -> new HorarioAtendimento(null, contaId, j.diaSemana(),
                        j.horaInicio(), j.horaFim()))
                .toList();
        return horarioRepository.substituirDaConta(contaId, horarios);
    }
}
