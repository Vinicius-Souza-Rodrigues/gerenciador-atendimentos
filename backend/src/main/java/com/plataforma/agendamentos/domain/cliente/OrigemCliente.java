package com.plataforma.agendamentos.domain.cliente;

/** De onde veio o cliente: do bot (identificado pelo Telegram) ou do cadastro manual (telefone). */
public enum OrigemCliente {
    BOT,
    MANUAL
}
