package com.plataforma.agendamentos.domain.cliente;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ClienteTest {

    private static final long CONTA = 10L;

    @Test
    void cliente_do_bot_so_com_telegram_id_e_valido() {
        assertThatCode(() -> new Cliente(1L, CONTA, "Ana", 12345L, null, OrigemCliente.BOT, null))
                .doesNotThrowAnyException();
    }

    @Test
    void cliente_manual_so_com_telefone_e_valido() {
        assertThatCode(() -> new Cliente(1L, CONTA, "Ana", null, "11999998888", OrigemCliente.MANUAL, null))
                .doesNotThrowAnyException();
    }

    @Test
    void cliente_sem_telegram_e_sem_telefone_e_invalido() {
        assertThatThrownBy(() -> new Cliente(1L, CONTA, "Ana", null, null, OrigemCliente.BOT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("telegramUserId OU telefone");
    }
}
