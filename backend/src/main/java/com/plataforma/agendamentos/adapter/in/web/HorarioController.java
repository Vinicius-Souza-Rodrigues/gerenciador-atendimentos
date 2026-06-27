package com.plataforma.agendamentos.adapter.in.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.agendamentos.adapter.in.web.dto.HorarioDtos.HorarioRequest;
import com.plataforma.agendamentos.adapter.in.web.dto.HorarioDtos.HorarioResponse;
import com.plataforma.agendamentos.application.port.in.GerenciarHorarioUseCase;
import com.plataforma.agendamentos.application.port.in.GerenciarHorarioUseCase.JanelaCommand;

import jakarta.validation.Valid;

/** Janelas de atendimento (PROTEGIDO por JWT; escopado pela conta do token). */
@RestController
@RequestMapping("/api/horarios")
class HorarioController {

    private final GerenciarHorarioUseCase horarios;

    HorarioController(GerenciarHorarioUseCase horarios) {
        this.horarios = horarios;
    }

    @GetMapping
    List<HorarioResponse> listar(@AuthenticationPrincipal Long contaId) {
        return horarios.listar(contaId).stream().map(HorarioResponse::de).toList();
    }

    @PutMapping
    List<HorarioResponse> definir(@AuthenticationPrincipal Long contaId,
                                  @Valid @RequestBody List<HorarioRequest> req) {
        List<JanelaCommand> janelas = req.stream()
                .map(h -> new JanelaCommand(h.diaSemana(), h.horaInicio(), h.horaFim()))
                .toList();
        return horarios.definir(contaId, janelas).stream().map(HorarioResponse::de).toList();
    }
}
