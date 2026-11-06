package br.edu.campusmarket.usuarios.infrastructure;

import br.edu.campusmarket.usuarios.domain.*;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserAccounts implements UserAccounts {
    private final UserJpaRepository repository;
    public JpaUserAccounts(UserJpaRepository repository) { this.repository = repository; }
    public boolean existsByRa(String ra) { return repository.existsByRa(ra); }
    public AppUser save(AppUser user) { return repository.save(user); }
    public Optional<AppUser> findByRa(String ra) { return repository.findByRa(ra); }
}
