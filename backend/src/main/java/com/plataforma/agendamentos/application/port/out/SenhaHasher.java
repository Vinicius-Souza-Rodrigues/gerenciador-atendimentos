package com.plataforma.agendamentos.application.port.out;

/**
 * Port de saída para hashing de senha. A implementação (BCrypt) mora no adapter — a
 * aplicação não conhece o algoritmo.
 */
public interface SenhaHasher {

    String hash(String senhaPura);

    boolean confere(String senhaPura, String hash);
}
