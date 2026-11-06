package br.edu.campusmarket.usuarios.infrastructure;

import br.edu.campusmarket.usuarios.domain.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {
    private final PasswordEncoder encoder;
    public BCryptPasswordHasher(PasswordEncoder encoder) { this.encoder = encoder; }
    public String hash(String rawPassword) { return encoder.encode(rawPassword); }
}
