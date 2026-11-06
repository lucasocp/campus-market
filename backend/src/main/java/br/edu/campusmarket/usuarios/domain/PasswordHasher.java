package br.edu.campusmarket.usuarios.domain;

/** Contrato mínimo necessário ao cadastro (ISP). */
public interface PasswordHasher {
    String hash(String rawPassword);
}
