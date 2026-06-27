package com.plataforma.agendamentos.adapter.in.telegram;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class BotSessionStore {

    private final ConcurrentHashMap<Long, BotSession> sessions = new ConcurrentHashMap<>();

    Optional<BotSession> buscar(Long telegramUserId) {
        return Optional.ofNullable(sessions.get(telegramUserId));
    }

    void salvar(Long telegramUserId, BotSession session) {
        sessions.put(telegramUserId, session);
    }

    void remover(Long telegramUserId) {
        sessions.remove(telegramUserId);
    }
}
