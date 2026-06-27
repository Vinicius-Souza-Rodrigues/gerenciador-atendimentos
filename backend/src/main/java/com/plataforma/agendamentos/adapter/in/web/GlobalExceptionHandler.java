package com.plataforma.agendamentos.adapter.in.web;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.plataforma.agendamentos.application.exception.CredenciaisInvalidasException;
import com.plataforma.agendamentos.application.exception.EmailJaCadastradoException;
import com.plataforma.agendamentos.application.exception.RecursoNaoEncontradoException;
import com.plataforma.agendamentos.domain.agendamento.ConflitoDeAgendamentoException;
import com.plataforma.agendamentos.domain.agendamento.HorarioIndisponivelException;

/** Converte exceções de domínio/aplicação em respostas HTTP padronizadas {erro, mensagem}. */
@RestControllerAdvice
class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> corpo(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", erro, "mensagem", mensagem));
    }

    @ExceptionHandler(EmailJaCadastradoException.class)
    ResponseEntity<Map<String, String>> emailDuplicado(EmailJaCadastradoException e) {
        return corpo(HttpStatus.CONFLICT, "email_ja_cadastrado", e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    ResponseEntity<Map<String, String>> credenciais(CredenciaisInvalidasException e) {
        return corpo(HttpStatus.UNAUTHORIZED, "credenciais_invalidas", e.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<Map<String, String>> naoEncontrado(RecursoNaoEncontradoException e) {
        return corpo(HttpStatus.NOT_FOUND, "nao_encontrado", e.getMessage());
    }

    @ExceptionHandler(ConflitoDeAgendamentoException.class)
    ResponseEntity<Map<String, String>> conflito(ConflitoDeAgendamentoException e) {
        return corpo(HttpStatus.CONFLICT, "conflito_de_agendamento", e.getMessage());
    }

    @ExceptionHandler(HorarioIndisponivelException.class)
    ResponseEntity<Map<String, String>> indisponivel(HorarioIndisponivelException e) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "horario_indisponivel", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalido(IllegalArgumentException e) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "dados_invalidos", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
        String detalhe = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return corpo(HttpStatus.BAD_REQUEST, "validacao", detalhe);
    }
}
