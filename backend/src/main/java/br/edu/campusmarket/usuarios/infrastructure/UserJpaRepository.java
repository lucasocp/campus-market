package br.edu.campusmarket.usuarios.infrastructure;

import br.edu.campusmarket.usuarios.domain.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByRa(String ra);
    boolean existsByRa(String ra);
}
