package com.plataforma.agendamentos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plataforma.agendamentos.application.exception.CredenciaisInvalidasException;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.application.port.out.SenhaHasher;
import com.plataforma.agendamentos.domain.conta.Conta;

@ExtendWith(MockitoExtension.class)
class AutenticarServiceTest {

    @Mock ContaRepository contaRepository;
    @Mock SenhaHasher senhaHasher;
    @InjectMocks AutenticarService service;

    private Conta conta() {
        return new Conta(1L, "Ana", "ana@x.com", "HASH", "tok", null);
    }

    @Test
    void autentica_com_credenciais_validas() {
        when(contaRepository.buscarPorEmail("ana@x.com")).thenReturn(Optional.of(conta()));
        when(senhaHasher.confere("segredo1", "HASH")).thenReturn(true);

        assertThat(service.autenticar("Ana@X.com", "segredo1").id()).isEqualTo(1L);
    }

    @Test
    void falha_com_email_inexistente() {
        when(contaRepository.buscarPorEmail("nao@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("nao@x.com", "x"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void falha_com_senha_errada() {
        when(contaRepository.buscarPorEmail("ana@x.com")).thenReturn(Optional.of(conta()));
        when(senhaHasher.confere("errada", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> service.autenticar("ana@x.com", "errada"))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }
}
