package com.plataforma.agendamentos.domain.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.plataforma.agendamentos.domain.servico.Servico;

class AgendamentoTest {

    private static final long CONTA = 10L;
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 6, 29, 10, 0);
    private static final Instant AGORA = Instant.parse("2026-06-26T08:00:00Z");

    private Servico servico(long contaId, int duracaoMin, BigDecimal preco) {
        return new Servico(1L, contaId, "Corte", duracaoMin, "desc", preco, true);
    }

    @Test
    void confirmar_calcula_fim_a_partir_da_duracao_e_nasce_confirmado() {
        Agendamento ag = Agendamento.confirmar(CONTA, 5L, servico(CONTA, 30, new BigDecimal("50.00")),
                INICIO, OrigemAgendamento.BOT, AGORA);

        assertThat(ag.inicio()).isEqualTo(INICIO);
        assertThat(ag.fim()).isEqualTo(INICIO.plusMinutes(30));
        assertThat(ag.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(ag.precoCobrado()).isEqualByComparingTo("50.00"); // snapshot do preço
    }

    @Test
    void confirmar_com_servico_de_outra_conta_falha() {
        assertThatThrownBy(() -> Agendamento.confirmar(CONTA, 5L, servico(99L, 30, null),
                INICIO, OrigemAgendamento.BOT, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mesma conta");
    }

    @Test
    void remarcar_move_o_mesmo_registro_mantendo_id() {
        Agendamento ag = new Agendamento(7L, CONTA, 5L, 1L,
                INICIO, INICIO.plusMinutes(30), null,
                StatusAgendamento.CONFIRMADO, OrigemAgendamento.BOT, AGORA, AGORA);

        LocalDateTime novo = INICIO.with(LocalTime.of(11, 0));
        ag.remarcar(novo, servico(CONTA, 30, null), Instant.parse("2026-06-26T09:00:00Z"));

        assertThat(ag.id()).isEqualTo(7L);
        assertThat(ag.inicio()).isEqualTo(novo);
        assertThat(ag.fim()).isEqualTo(novo.plusMinutes(30));
        assertThat(ag.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void nao_remarca_agendamento_cancelado() {
        Agendamento ag = new Agendamento(7L, CONTA, 5L, 1L,
                INICIO, INICIO.plusMinutes(30), null,
                StatusAgendamento.CANCELADO, OrigemAgendamento.BOT, AGORA, AGORA);

        assertThatThrownBy(() -> ag.remarcar(INICIO.plusHours(1), servico(CONTA, 30, null), AGORA))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelar_muda_status_e_nao_apaga() {
        Agendamento ag = Agendamento.confirmar(CONTA, 5L, servico(CONTA, 30, null),
                INICIO, OrigemAgendamento.BOT, AGORA);

        ag.cancelar(AGORA);

        assertThat(ag.status()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(ag.estaConfirmado()).isFalse();
    }
}
