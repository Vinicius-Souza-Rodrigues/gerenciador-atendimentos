package com.plataforma.agendamentos.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.application.port.in.ObterContaUseCase;
import com.plataforma.agendamentos.application.port.out.ContaRepository;
import com.plataforma.agendamentos.domain.conta.Conta;

@Service
public class ObterContaService implements ObterContaUseCase {

    private final ContaRepository contaRepository;

    public ObterContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Conta obter(Long contaId) {
        return contaRepository.buscarPorId(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
    }
}
