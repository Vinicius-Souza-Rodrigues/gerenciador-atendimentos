package com.plataforma.agendamentos.domain.agendamento;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;
import com.plataforma.agendamentos.domain.servico.Servico;

class RegrasDeAgendamentoTest {

    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-06-26T08:00:00Z"), ZoneOffset.UTC);
    private static final long CONTA = 10L;
    private static final LocalDateTime SEGUNDA_10H = LocalDateTime.of(2026, 6, 29, 10, 0);

    private final RegrasDeAgendamento regras = new RegrasDeAgendamento(RELOGIO);
    private final Servico servico = new Servico(1L, CONTA, "Corte", 30, "desc", null, true);
    private final List<HorarioAtendimento> janelaSegunda = List.of(
            new HorarioAtendimento(1L, CONTA, DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0)));

    @Test
    void horario_valido_nao_lanca() {
        assertThatCode(() -> regras.validar(servico, SEGUNDA_10H, janelaSegunda, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void horario_no_passado_e_indisponivel() {
        LocalDateTime passado = LocalDateTime.of(2026, 6, 22, 10, 0); // segunda anterior
        assertThatThrownBy(() -> regras.validar(servico, passado, janelaSegunda, List.of()))
                .isInstanceOf(HorarioIndisponivelException.class)
                .hasMessageContaining("passado");
    }

    @Test
    void horario_fora_da_janela_e_indisponivel() {
        LocalDateTime forajanela = LocalDateTime.of(2026, 6, 29, 13, 0); // depois das 12h
        assertThatThrownBy(() -> regras.validar(servico, forajanela, janelaSegunda, List.of()))
                .isInstanceOf(HorarioIndisponivelException.class)
                .hasMessageContaining("janela");
    }

    @Test
    void slot_que_estoura_o_fim_da_janela_e_indisponivel() {
        LocalDateTime quaseFim = LocalDateTime.of(2026, 6, 29, 11, 45); // 11:45+30 = 12:15 > 12:00
        assertThatThrownBy(() -> regras.validar(servico, quaseFim, janelaSegunda, List.of()))
                .isInstanceOf(HorarioIndisponivelException.class);
    }

    @Test
    void sobreposicao_com_confirmado_gera_conflito() {
        Agendamento confirmado = new Agendamento(99L, CONTA, 5L, 1L,
                SEGUNDA_10H, SEGUNDA_10H.plusMinutes(30), null,
                StatusAgendamento.CONFIRMADO, OrigemAgendamento.BOT, null, null);

        assertThatThrownBy(() -> regras.validar(servico, SEGUNDA_10H, janelaSegunda, List.of(confirmado)))
                .isInstanceOf(ConflitoDeAgendamentoException.class);
    }
}
