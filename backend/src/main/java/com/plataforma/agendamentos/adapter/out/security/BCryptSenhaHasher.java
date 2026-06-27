package com.plataforma.agendamentos.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.plataforma.agendamentos.application.port.out.SenhaHasher;

/** Implementação do port {@link SenhaHasher} usando BCrypt. */
@Component
class BCryptSenhaHasher implements SenhaHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String senhaPura) {
        return encoder.encode(senhaPura);
    }

    @Override
    public boolean confere(String senhaPura, String hash) {
        return encoder.matches(senhaPura, hash);
    }
}
