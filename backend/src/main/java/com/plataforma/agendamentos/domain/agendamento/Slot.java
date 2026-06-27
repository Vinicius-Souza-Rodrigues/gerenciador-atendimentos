package com.plataforma.agendamentos.domain.agendamento;

import java.time.LocalDateTime;

/**
 * Um intervalo candidato a agendamento: [inicio, fim). Value object imutável e calculado
 * (não persistido). A sobreposição usa intervalo semiaberto: encostar não conflita.
 */
public record Slot(LocalDateTime inicio, LocalDateTime fim) {

    public Slot {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Slot precisa de início e fim.");
        }
        if (!fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Fim do slot deve ser depois do início.");
        }
    }

    /** Dois intervalos [a) e [b) se sobrepõem quando aInicio &lt; bFim E bInicio &lt; aFim. */
    public boolean sobrepoe(LocalDateTime outroInicio, LocalDateTime outroFim) {
        return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }

    public boolean sobrepoe(Slot outro) {
        return sobrepoe(outro.inicio, outro.fim);
    }
}
