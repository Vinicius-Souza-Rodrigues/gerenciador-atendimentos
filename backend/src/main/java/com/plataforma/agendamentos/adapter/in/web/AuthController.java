package com.plataforma.agendamentos.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma.agendamentos.adapter.in.web.dto.AuthDtos.LoginRequest;
import com.plataforma.agendamentos.adapter.in.web.dto.AuthDtos.LoginResponse;
import com.plataforma.agendamentos.adapter.in.web.dto.AuthDtos.SignupRequest;
import com.plataforma.agendamentos.adapter.in.web.dto.AuthDtos.SignupResponse;
import com.plataforma.agendamentos.adapter.in.web.security.JwtService;
import com.plataforma.agendamentos.application.port.in.AutenticarUseCase;
import com.plataforma.agendamentos.application.port.in.CriarContaUseCase;
import com.plataforma.agendamentos.application.port.in.CriarContaUseCase.CriarContaCommand;
import com.plataforma.agendamentos.domain.conta.Conta;

import jakarta.validation.Valid;

/** Endpoints PÚBLICOS de autenticação: signup e login. */
@RestController
@RequestMapping("/api/auth")
class AuthController {

    private final CriarContaUseCase criarConta;
    private final AutenticarUseCase autenticar;
    private final JwtService jwtService;
    private final DeepLinkBuilder deepLinkBuilder;

    AuthController(CriarContaUseCase criarConta, AutenticarUseCase autenticar,
                  JwtService jwtService, DeepLinkBuilder deepLinkBuilder) {
        this.criarConta = criarConta;
        this.autenticar = autenticar;
        this.jwtService = jwtService;
        this.deepLinkBuilder = deepLinkBuilder;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    SignupResponse signup(@Valid @RequestBody SignupRequest req) {
        Conta conta = criarConta.criar(new CriarContaCommand(req.nome(), req.email(), req.senha()));
        return new SignupResponse(conta.id(), deepLinkBuilder.paraToken(conta.botDeepLinkToken()));
    }

    @PostMapping("/login")
    LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Conta conta = autenticar.autenticar(req.email(), req.senha());
        return new LoginResponse(jwtService.gerarToken(conta.id(), conta.email()));
    }
}
