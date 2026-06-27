package com.plataforma.agendamentos.adapter.in.telegram;

import java.time.LocalDate;

record BotSession(
        Long contaId,
        BotStep step,
        Long servicoId,
        Long agendamentoId,
        LocalDate data) {

    static BotSession inicial(Long contaId) {
        return new BotSession(contaId, BotStep.AGUARDANDO_SERVICO, null, null, null);
    }

    BotSession comStep(BotStep novoStep) {
        return new BotSession(contaId, novoStep, servicoId, agendamentoId, data);
    }

    BotSession comServico(Long novoServicoId) {
        return new BotSession(contaId, BotStep.AGUARDANDO_DATA, novoServicoId, null, null);
    }

    BotSession comData(LocalDate novaData) {
        return new BotSession(contaId, BotStep.AGUARDANDO_SLOT, servicoId, null, novaData);
    }

    BotSession comAgendamento(Long novoAgendamentoId, BotStep novoStep) {
        return new BotSession(contaId, novoStep, null, novoAgendamentoId, null);
    }

    BotSession comNovaData(Long servicoParaRemarcar, LocalDate novaData) {
        return new BotSession(contaId, BotStep.AGUARDANDO_REMARCAR_NOVO_SLOT,
                servicoParaRemarcar, agendamentoId, novaData);
    }
}
