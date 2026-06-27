package com.plataforma.agendamentos.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Monta o deep link compartilhável do bot: {@code <base>/<username>?start=<token>}. */
@Component
class DeepLinkBuilder {

    private final String base;
    private final String username;

    DeepLinkBuilder(
            @Value("${telegram.bot.deep-link-base:https://t.me}") String base,
            @Value("${telegram.bot.username:}") String username) {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        this.username = username;
    }

    String paraToken(String token) {
        String user = (username == null || username.isBlank()) ? "SeuBot" : username;
        return base + "/" + user + "?start=" + token;
    }
}
