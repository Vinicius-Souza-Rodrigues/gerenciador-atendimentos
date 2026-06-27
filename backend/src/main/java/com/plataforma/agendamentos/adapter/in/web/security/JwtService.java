package com.plataforma.agendamentos.adapter.in.web.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Gera e valida o JWT da área web. Infra (não conhecida pela aplicação/domínio). */
@Component
public class JwtService {

    private final SecretKey chave;
    private final long validadeSegundos;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds:86400}") long validadeSegundos) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validadeSegundos = validadeSegundos;
    }

    public String gerarToken(Long contaId, String email) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(contaId))
                .claim("email", email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(validadeSegundos)))
                .signWith(chave)
                .compact();
    }

    /** Valida a assinatura/expiração e devolve o contaId do subject. */
    public Long extrairContaId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
