package com.plataforma.agendamentos.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plataforma.agendamentos.application.exception.EmailJaCadastradoException;
import com.plataforma.agendamentos.application.port.in.CriarContaUseCase.CriarContaCommand;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.application.port.out.SenhaHasher;
import com.plataforma.agendamentos.domain.conta.Conta;

@ExtendWith(MockitoExtension.class)
class CriarContaServiceTest {

    @Mock ContaRepository contaRepository;
    @Mock SenhaHasher senhaHasher;
    @InjectMocks CriarContaService service;

    @Test
    void criar_falha_se_email_ja_existe() {
        when(contaRepository.existeComEmail("ana@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CriarContaCommand("Ana", "ana@x.com", "segredo1")))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    void criar_hasheia_senha_gera_token_e_salva() {
        when(contaRepository.existeComEmail(any())).thenReturn(false);
        when(senhaHasher.hash("segredo1")).thenReturn("HASH");
        when(contaRepository.salvar(any())).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            return new Conta(1L, c.nome(), c.email(), c.senhaHash(), c.botDeepLinkToken(), c.criadoEm());
        });

        Conta conta = service.criar(new CriarContaCommand("Ana", "Ana@X.com", "segredo1"));

        assertThat(conta.id()).isEqualTo(1L);
        assertThat(conta.email()).isEqualTo("ana@x.com"); // normalizado
        assertThat(conta.senhaHash()).isEqualTo("HASH");
        assertThat(conta.botDeepLinkToken()).isNotBlank();
    }
}
