package com.plataforma.agendamentos.adapter.in.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.agendamentos.adapter.in.web.dto.AgendaDtos;
import com.plataforma.agendamentos.application.port.in.CadastrarClienteManualUseCase;
import com.plataforma.agendamentos.application.port.in.CancelarAgendamentoUseCase;
import com.plataforma.agendamentos.application.port.in.VerAgendaUseCase;
import com.plataforma.agendamentos.domain.agendamento.Agendamento;

import jakarta.validation.Valid;

/** Endpoints da visão web da agenda (protegidos por JWT). */
@RestController
class AgendaController {

    private final VerAgendaUseCase verAgenda;
    private final CancelarAgendamentoUseCase cancelar;
    private final CadastrarClienteManualUseCase cadastrarManual;

    AgendaController(VerAgendaUseCase verAgenda,
                     CancelarAgendamentoUseCase cancelar,
                     CadastrarClienteManualUseCase cadastrarManual) {
        this.verAgenda = verAgenda;
        this.cancelar = cancelar;
        this.cadastrarManual = cadastrarManual;
    }

    /** Resumo por dia: quantos agendamentos confirmados cada dia do período tem. */
    @GetMapping("/api/agenda")
    List<AgendaDtos.ResumoDiaResponse> resumo(
            @AuthenticationPrincipal Long contaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return verAgenda.resumoPorDia(contaId, de, ate).stream()
                .map(AgendaDtos.ResumoDiaResponse::de).toList();
    }

    /** Lista completa (com nome do cliente e serviço) de um dia específico. */
    @GetMapping("/api/agendamentos")
    List<AgendaDtos.ItemDoDiaResponse> listarDia(
            @AuthenticationPrincipal Long contaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return verAgenda.listarDoDia(contaId, data).stream()
                .map(AgendaDtos.ItemDoDiaResponse::de).toList();
    }

    /** Cancela um agendamento pelo painel web. */
    @DeleteMapping("/api/agendamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancelarAgendamento(@AuthenticationPrincipal Long contaId, @PathVariable Long id) {
        cancelar.cancelar(contaId, id);
    }

    /** Cadastra cliente manualmente e cria um agendamento imediatamente. */
    @PostMapping("/api/clientes/manual")
    @ResponseStatus(HttpStatus.CREATED)
    AgendaDtos.AgendamentoResponse novoManual(
            @AuthenticationPrincipal Long contaId,
            @Valid @RequestBody AgendaDtos.CadastroManualRequest req) {
        Agendamento a = cadastrarManual.cadastrar(
                new CadastrarClienteManualUseCase.CadastrarClienteManualCommand(
                        contaId, req.nomeCliente(), req.telefone(), req.servicoId(), req.inicio()));
        return AgendaDtos.AgendamentoResponse.de(a);
    }
}
