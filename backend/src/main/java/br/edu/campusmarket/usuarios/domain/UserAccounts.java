package br.edu.campusmarket.usuarios.domain;

import java.util.Optional;

/** Porta de persistência do cadastro, independente de Spring Data (DIP). */
public interface UserAccounts {
    boolean existsByRa(String ra);
    AppUser save(AppUser user);
    Optional<AppUser> findByRa(String ra);
}
