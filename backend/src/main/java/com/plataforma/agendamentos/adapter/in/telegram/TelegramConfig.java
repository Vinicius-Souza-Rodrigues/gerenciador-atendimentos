package com.plataforma.agendamentos.adapter.in.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramConfig {

    private static final Logger log = LoggerFactory.getLogger(TelegramConfig.class);

    @Bean
    public TelegramClient telegramClient(@Value("${telegram.bot.token:}") String token) {
        return new OkHttpTelegramClient(token.isBlank() ? "placeholder" : token);
    }

    @Bean
    public ApplicationRunner telegramBotRunner(
            @Value("${telegram.bot.token:}") String token,
            TelegramBotAdapter botAdapter) {
        return args -> {
            if (token == null || token.isBlank()) {
                log.warn("TELEGRAM_BOT_TOKEN não configurado — bot não iniciado.");
                return;
            }
            TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
            app.registerBot(token, botAdapter);
            log.info("Bot Telegram iniciado com long polling.");
        };
    }
}
