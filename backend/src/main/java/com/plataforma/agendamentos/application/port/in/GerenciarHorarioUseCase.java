package com.plataforma.agendamentos.application.port.in;

import java.time.LocalTime;
import java.util.List;

import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;

/** Define e consulta as janelas de atendimento (HorarioAtendimento) da conta. */
public interface GerenciarHorarioUseCase {

    List<HorarioAtendimento> listar(Long contaId);

    /** Substitui todas as janelas da conta (replace-all). */
    List<HorarioAtendimento> definir(Long contaId, List<JanelaCommand> janelas);

    record JanelaCommand(DiaSemana diaSemana, LocalTime horaInicio, LocalTime horaFim) {
    }
}
