package com.plataforma.agendamentos.application.port.in;

public interface CancelarAgendamentoUseCase {

    void cancelar(Long contaId, Long agendamentoId);
}
