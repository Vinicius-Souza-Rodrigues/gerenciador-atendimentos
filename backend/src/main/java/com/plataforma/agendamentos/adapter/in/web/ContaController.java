package com.plataforma.agendamentos.adapter.in.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.agendamentos.adapter.in.web.dto.ContaDtos.BotLinkResponse;
import com.plataforma.agendamentos.application.port.in.ObterContaUseCase;
import com.plataforma.agendamentos.domain.conta.Conta;

/** Dados da conta autenticada (PROTEGIDO por JWT). */
@RestController
@RequestMapping("/api/conta")
class ContaController {

    private final ObterContaUseCase obterConta;
    private final DeepLinkBuilder deepLinkBuilder;

    ContaController(ObterContaUseCase obterConta, DeepLinkBuilder deepLinkBuilder) {
        this.obterConta = obterConta;
        this.deepLinkBuilder = deepLinkBuilder;
    }

    @GetMapping("/bot-link")
    BotLinkResponse botLink(@AuthenticationPrincipal Long contaId) {
        Conta conta = obterConta.obter(contaId);
        return new BotLinkResponse(deepLinkBuilder.paraToken(conta.botDeepLinkToken()));
    }
}
