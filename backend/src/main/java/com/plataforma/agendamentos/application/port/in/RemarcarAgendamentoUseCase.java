package com.plataforma.agendamentos.application.port.in;

import java.time.LocalDateTime;

public interface RemarcarAgendamentoUseCase {

    void remarcar(Long contaId, Long agendamentoId, LocalDateTime novoInicio);
}
