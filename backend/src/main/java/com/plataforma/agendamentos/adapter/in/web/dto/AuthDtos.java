package com.plataforma.agendamentos.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de autenticação (entrada/saída do adapter web). */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignupRequest(
            @NotBlank String nome,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres") String senha) {
    }

    public record SignupResponse(Long contaId, String botDeepLink) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String senha) {
    }

    public record LoginResponse(String token) {
    }
}
