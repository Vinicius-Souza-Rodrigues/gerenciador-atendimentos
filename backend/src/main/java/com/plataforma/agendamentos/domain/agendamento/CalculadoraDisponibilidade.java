package com.plataforma.agendamentos.domain.agendamento;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;
import com.plataforma.agendamentos.domain.servico.Servico;

/**
 * Calcula a disponibilidade de uma conta para um serviço. Serviço de domínio puro.
 *
 * <p>Slots são gerados <b>encadeados pela duração do serviço</b> a partir do início de cada
 * janela de atendimento. Um slot só "cabe" se {@code inicio + duracao <= horaFim}. Um slot é
 * <b>livre</b> quando é futuro (em relação ao {@link Clock} injetado) e não se sobrepõe a
 * nenhum agendamento CONFIRMADO da conta.
 */
public class CalculadoraDisponibilidade {

    private final Clock clock;

    public CalculadoraDisponibilidade(Clock clock) {
        this.clock = clock;
    }

    /** Slots livres do serviço no intervalo [de, ate] (inclusive), ordenados por início. */
    public List<Slot> slotsLivres(Servico servico, List<HorarioAtendimento> horarios,
                                  List<Agendamento> confirmados, LocalDate de, LocalDate ate) {
        if (de.isAfter(ate)) {
            throw new IllegalArgumentException("Intervalo inválido: 'de' depois de 'ate'.");
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Slot> livres = new ArrayList<>();

        for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
            DiaSemana diaSemana = DiaSemana.de(dia.getDayOfWeek());
            for (HorarioAtendimento horario : horarios) {
                if (horario.diaSemana() != diaSemana) {
                    continue;
                }
                for (Slot slot : slotsDoDia(servico, horario, dia)) {
                    if (!slot.inicio().isAfter(agora)) {
                        continue; // só horário futuro
                    }
                    if (temConflito(slot, servico.contaId(), confirmados)) {
                        continue; // não pode sobrepor CONFIRMADO
                    }
                    livres.add(slot);
                }
            }
        }
        livres.sort(Comparator.comparing(Slot::inicio));
        return livres;
    }

    /**
     * Gera todos os slots encadeados de uma janela num dia, sem filtrar (livres ou não).
     * Exposto para testar a regra de geração isoladamente.
     */
    public List<Slot> slotsDoDia(Servico servico, HorarioAtendimento horario, LocalDate dia) {
        List<Slot> slots = new ArrayList<>();
        Duration duracao = servico.duracao();
        LocalDateTime inicio = LocalDateTime.of(dia, horario.horaInicio());
        LocalDateTime fimJanela = LocalDateTime.of(dia, horario.horaFim());

        while (!inicio.plus(duracao).isAfter(fimJanela)) {
            slots.add(new Slot(inicio, inicio.plus(duracao)));
            inicio = inicio.plus(duracao);
        }
        return slots;
    }

    private boolean temConflito(Slot slot, Long contaId, List<Agendamento> confirmados) {
        return confirmados.stream()
                .filter(Agendamento::estaConfirmado)
                .filter(a -> contaId.equals(a.contaId()))
                .anyMatch(a -> slot.sobrepoe(a.inicio(), a.fim()));
    }
}
