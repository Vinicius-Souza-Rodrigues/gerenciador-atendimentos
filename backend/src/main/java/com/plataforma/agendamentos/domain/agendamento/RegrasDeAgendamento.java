package com.plataforma.agendamentos.domain.agendamento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.plataforma.agendamentos.domain.horario.DiaSemana;
import com.plataforma.agendamentos.domain.horario.HorarioAtendimento;
import com.plataforma.agendamentos.domain.servico.Servico;

/**
 * Valida as regras contextuais de um agendamento antes de confirmá-lo:
 * horário <b>futuro</b>, <b>dentro de alguma janela</b> de atendimento e <b>sem sobreposição</b>
 * com agendamentos CONFIRMADOS da conta. Serviço de domínio puro.
 */
public class RegrasDeAgendamento {

    private final Clock clock;

    public RegrasDeAgendamento(Clock clock) {
        this.clock = clock;
    }

    public void validar(Servico servico, LocalDateTime inicio,
                        List<HorarioAtendimento> horarios, List<Agendamento> confirmados) {
        LocalDateTime fim = inicio.plus(servico.duracao());
        LocalDateTime agora = LocalDateTime.now(clock);

        if (!inicio.isAfter(agora)) {
            throw new HorarioIndisponivelException("Horário no passado.");
        }
        if (!dentroDeAlgumaJanela(inicio, fim, horarios)) {
            throw new HorarioIndisponivelException("Fora da janela de atendimento da conta.");
        }

        Slot candidato = new Slot(inicio, fim);
        boolean conflito = confirmados.stream()
                .filter(Agendamento::estaConfirmado)
                .filter(a -> servico.contaId().equals(a.contaId()))
                .anyMatch(a -> candidato.sobrepoe(a.inicio(), a.fim()));
        if (conflito) {
            throw new ConflitoDeAgendamentoException("Horário já ocupado por outro agendamento.");
        }
    }

    private boolean dentroDeAlgumaJanela(LocalDateTime inicio, LocalDateTime fim,
                                         List<HorarioAtendimento> horarios) {
        DiaSemana diaSemana = DiaSemana.de(inicio.toLocalDate().getDayOfWeek());
        return horarios.stream()
                .filter(h -> h.diaSemana() == diaSemana)
                .anyMatch(h -> {
                    LocalDateTime janelaInicio = LocalDateTime.of(inicio.toLocalDate(), h.horaInicio());
                    LocalDateTime janelaFim = LocalDateTime.of(inicio.toLocalDate(), h.horaFim());
                    return !inicio.isBefore(janelaInicio) && !fim.isAfter(janelaFim);
                });
    }
}
