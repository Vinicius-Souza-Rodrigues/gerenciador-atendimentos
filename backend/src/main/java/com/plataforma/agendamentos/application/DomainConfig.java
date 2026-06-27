package com.plataforma.agendamentos.application;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plataforma.agendamentos.domain.agendamento.CalculadoraDisponibilidade;
import com.plataforma.agendamentos.domain.agendamento.RegrasDeAgendamento;

@Configuration
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CalculadoraDisponibilidade calculadoraDisponibilidade(Clock clock) {
        return new CalculadoraDisponibilidade(clock);
    }

    @Bean
    public RegrasDeAgendamento regrasDeAgendamento(Clock clock) {
        return new RegrasDeAgendamento(clock);
    }
}
