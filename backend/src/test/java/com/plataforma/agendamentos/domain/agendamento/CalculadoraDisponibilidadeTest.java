package com.plataforma.agendamentos.domain.agendamento;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;
import com.plataforma.agendamentos.domain.servico.Servico;

class CalculadoraDisponibilidadeTest {

    // "agora" fixo: sexta, 2026-06-26 08:00 UTC.
    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-06-26T08:00:00Z"), ZoneOffset.UTC);
    private static final long CONTA = 10L;
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 6, 29); // futura
    private static final LocalDate SEXTA_HOJE = LocalDate.of(2026, 6, 26);

    private final CalculadoraDisponibilidade calc = new CalculadoraDisponibilidade(RELOGIO);

    private Servico servico(int duracaoMin) {
        return new Servico(1L, CONTA, "Corte", duracaoMin, "desc", new BigDecimal("50.00"), true);
    }

    private HorarioAtendimento horario(DiaSemana dia, LocalTime ini, LocalTime fim) {
        return new HorarioAtendimento(1L, CONTA, dia, ini, fim);
    }

    @Test
    void gera_slots_encadeados_pela_duracao_do_servico() {
        var slots = calc.slotsDoDia(servico(30),
                horario(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0)), SEGUNDA);

        assertThat(slots).hasSize(6);
        assertThat(slots.get(0).inicio()).isEqualTo(LocalDateTime.of(SEGUNDA, LocalTime.of(9, 0)));
        assertThat(slots.get(0).fim()).isEqualTo(LocalDateTime.of(SEGUNDA, LocalTime.of(9, 30)));
        assertThat(slots.get(5).fim()).isEqualTo(LocalDateTime.of(SEGUNDA, LocalTime.of(12, 0)));
    }

    @Test
    void nao_gera_slot_que_estoura_a_janela() {
        // 45min em janela de 3h: 9:00, 9:45, 10:30, 11:15 (termina 12:00). 12:00 não cabe.
        var slots = calc.slotsDoDia(servico(45),
                horario(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0)), SEGUNDA);

        assertThat(slots).hasSize(4);
        assertThat(slots.get(3).fim()).isEqualTo(LocalDateTime.of(SEGUNDA, LocalTime.of(12, 0)));
    }

    @Test
    void filtra_slots_no_passado() {
        // Hoje (sexta) janela 07:00–09:00, agora 08:00 → só 08:30 é futuro.
        var livres = calc.slotsLivres(servico(30),
                List.of(horario(DiaSemana.SEXTA, LocalTime.of(7, 0), LocalTime.of(9, 0))),
                List.of(), SEXTA_HOJE, SEXTA_HOJE);

        assertThat(livres).hasSize(1);
        assertThat(livres.get(0).inicio()).isEqualTo(LocalDateTime.of(SEXTA_HOJE, LocalTime.of(8, 30)));
    }

    @Test
    void filtra_slots_que_sobrepoem_agendamento_confirmado() {
        Agendamento confirmado = new Agendamento(99L, CONTA, 5L, 1L,
                LocalDateTime.of(SEGUNDA, LocalTime.of(10, 0)),
                LocalDateTime.of(SEGUNDA, LocalTime.of(10, 30)),
                null, StatusAgendamento.CONFIRMADO, OrigemAgendamento.BOT, null, null);

        var livres = calc.slotsLivres(servico(30),
                List.of(horario(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0))),
                List.of(confirmado), SEGUNDA, SEGUNDA);

        assertThat(livres).hasSize(5);
        assertThat(livres).noneMatch(s ->
                s.inicio().equals(LocalDateTime.of(SEGUNDA, LocalTime.of(10, 0))));
    }

    @Test
    void agendamento_cancelado_nao_bloqueia_o_slot() {
        Agendamento cancelado = new Agendamento(99L, CONTA, 5L, 1L,
                LocalDateTime.of(SEGUNDA, LocalTime.of(10, 0)),
                LocalDateTime.of(SEGUNDA, LocalTime.of(10, 30)),
                null, StatusAgendamento.CANCELADO, OrigemAgendamento.BOT, null, null);

        var livres = calc.slotsLivres(servico(30),
                List.of(horario(DiaSemana.SEGUNDA, LocalTime.of(9, 0), LocalTime.of(12, 0))),
                List.of(cancelado), SEGUNDA, SEGUNDA);

        assertThat(livres).hasSize(6);
    }

    @Test
    void considera_apenas_horarios_do_dia_da_semana_correto() {
        // Janela só na TERCA; consultando a SEGUNDA → nada.
        var livres = calc.slotsLivres(servico(30),
                List.of(horario(DiaSemana.TERCA, LocalTime.of(9, 0), LocalTime.of(12, 0))),
                List.of(), SEGUNDA, SEGUNDA);

        assertThat(livres).isEmpty();
    }
}
