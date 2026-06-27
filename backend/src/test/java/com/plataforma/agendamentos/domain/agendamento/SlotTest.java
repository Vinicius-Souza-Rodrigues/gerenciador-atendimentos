package com.plataforma.agendamentos.domain.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class SlotTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 6, 29, 0, 0);

    private Slot slot(LocalTime ini, LocalTime fim) {
        return new Slot(DIA.with(ini), DIA.with(fim));
    }

    @Test
    void slot_com_fim_antes_do_inicio_e_invalido() {
        assertThatThrownBy(() -> slot(LocalTime.of(10, 0), LocalTime.of(9, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void slots_que_se_cruzam_sobrepoem() {
        assertThat(slot(LocalTime.of(10, 0), LocalTime.of(11, 0))
                .sobrepoe(slot(LocalTime.of(10, 30), LocalTime.of(11, 30)))).isTrue();
    }

    @Test
    void slots_que_apenas_encostam_nao_sobrepoem() {
        // [10:00,11:00) e [11:00,12:00) — borda compartilhada não conflita.
        assertThat(slot(LocalTime.of(10, 0), LocalTime.of(11, 0))
                .sobrepoe(slot(LocalTime.of(11, 0), LocalTime.of(12, 0)))).isFalse();
    }

    @Test
    void slots_disjuntos_nao_sobrepoem() {
        assertThat(slot(LocalTime.of(10, 0), LocalTime.of(11, 0))
                .sobrepoe(slot(LocalTime.of(14, 0), LocalTime.of(15, 0)))).isFalse();
    }
}
