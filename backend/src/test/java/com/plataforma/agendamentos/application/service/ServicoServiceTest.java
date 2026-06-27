package com.plataforma.agendamentos.application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.GerenciarServicoUseCase.AtualizarServicoCommand;
import com.plataforma.agendamentos.application.port.out.ServicoRepository;
import com.plataforma.agendamentos.domain.servico.Servico;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock ServicoRepository servicoRepository;
    @InjectMocks ServicoService service;

    private Servico servicoDaConta(long contaId) {
        return new Servico(1L, contaId, "Corte", 30, "desc", null, true);
    }

    @Test
    void remover_servico_de_outra_conta_trata_como_inexistente() {
        when(servicoRepository.buscarPorId(1L)).thenReturn(Optional.of(servicoDaConta(99L)));

        assertThatThrownBy(() -> service.remover(10L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(servicoRepository, never()).remover(1L);
    }

    @Test
    void atualizar_servico_de_outra_conta_falha() {
        when(servicoRepository.buscarPorId(1L)).thenReturn(Optional.of(servicoDaConta(99L)));

        assertThatThrownBy(() -> service.atualizar(
                new AtualizarServicoCommand(10L, 1L, "Novo", 45, "d", null, true)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void remover_servico_da_propria_conta_funciona() {
        when(servicoRepository.buscarPorId(1L)).thenReturn(Optional.of(servicoDaConta(10L)));

        assertThatCode(() -> service.remover(10L, 1L)).doesNotThrowAnyException();
        verify(servicoRepository).remover(1L);
    }
}
