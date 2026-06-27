package com.plataforma.agendamentos.application.port.out;

import java.util.List;

import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;

/** Port de saída para a disponibilidade configurada (HorarioAtendimento) da conta. */
public interface HorarioRepository {

    List<HorarioAtendimento> listarPorConta(Long contaId);

    /** Substitui todas as janelas da conta pela lista informada (replace-all). */
    List<HorarioAtendimento> substituirDaConta(Long contaId, List<HorarioAtendimento> horarios);
}
