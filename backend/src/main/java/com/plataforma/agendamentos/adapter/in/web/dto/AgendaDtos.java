package com.plataforma.agendamentos.adapter.in.web.dto;

import java.time.LocalDateTime;

import com.plataforma.agendamentos.application.port.in.VerAgendaUseCase;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AgendaDtos {

    public record ResumoDiaResponse(String data, int quantidade) {
        public static ResumoDiaResponse de(VerAgendaUseCase.ResumoDia r) {
            return new ResumoDiaResponse(r.data().toString(), r.quantidade());
        }
    }

    public record ItemDoDiaResponse(Long id, String inicio, String fim,
                                    String nomeCliente, String nomeServico,
                                    String status, String origem) {
        public static ItemDoDiaResponse de(VerAgendaUseCase.ItemDoDia item) {
            return new ItemDoDiaResponse(
                    item.id(), item.inicio().toString(), item.fim().toString(),
                    item.nomeCliente(), item.nomeServico(),
                    item.status().name(), item.origem().name());
        }
    }

    public record CadastroManualRequest(
            @NotBlank String nomeCliente,
            @NotBlank String telefone,
            @NotNull Long servicoId,
            @NotNull LocalDateTime inicio
    ) {}

    public record AgendamentoResponse(Long id, String inicio, String fim, String status) {
        public static AgendamentoResponse de(Agendamento a) {
            return new AgendamentoResponse(
                    a.id(), a.inicio().toString(), a.fim().toString(), a.status().name());
        }
    }
}
