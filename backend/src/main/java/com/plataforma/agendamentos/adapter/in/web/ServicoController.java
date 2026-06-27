package com.plataforma.agendamentos.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.agendamentos.adapter.in.web.dto.ServicoDtos.ServicoRequest;
import com.plataforma.agendamentos.adapter.in.web.dto.ServicoDtos.ServicoResponse;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase.AtualizarServicoCommand;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase.CriarServicoCommand;

import jakarta.validation.Valid;

/** CRUD de serviços (PROTEGIDO por JWT; escopado pela conta do token). */
@RestController
@RequestMapping("/api/servicos")
class ServicoController {

    private final GerenciarServicoUseCase servicos;

    ServicoController(GerenciarServicoUseCase servicos) {
        this.servicos = servicos;
    }

    @GetMapping
    List<ServicoResponse> listar(@AuthenticationPrincipal Long contaId) {
        return servicos.listar(contaId).stream().map(ServicoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ServicoResponse criar(@AuthenticationPrincipal Long contaId, @Valid @RequestBody ServicoRequest req) {
        var criado = servicos.criar(new CriarServicoCommand(contaId, req.nome(), req.duracaoMin(),
                req.descricao(), req.preco(), req.ativoOuPadrao()));
        return ServicoResponse.de(criado);
    }

    @PutMapping("/{id}")
    ServicoResponse atualizar(@AuthenticationPrincipal Long contaId, @PathVariable Long id,
                              @Valid @RequestBody ServicoRequest req) {
        var atualizado = servicos.atualizar(new AtualizarServicoCommand(contaId, id, req.nome(),
                req.duracaoMin(), req.descricao(), req.preco(), req.ativoOuPadrao()));
        return ServicoResponse.de(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remover(@AuthenticationPrincipal Long contaId, @PathVariable Long id) {
        servicos.remover(contaId, id);
    }
}
