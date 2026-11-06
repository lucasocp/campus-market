package br.edu.campusmarket.usuarios.application;

import br.edu.campusmarket.usuarios.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static br.edu.campusmarket.common.BusinessException.require;

/** Coordena cadastro; não conhece BCrypt, HTTP ou implementação do banco. */
@Service
public class RegisterUser {
    private final AcademicVerifier verifier;
    private final UserAccounts accounts;
    private final PasswordHasher passwords;

    public RegisterUser(AcademicVerifier verifier, UserAccounts accounts, PasswordHasher passwords) {
        this.verifier = verifier; this.accounts = accounts; this.passwords = passwords;
    }

    @Transactional
    public AppUser execute(String ra, String name, String course, String password, String invitation) {
        require(password != null && password.length() >= 8 && password.length() <= 64,
            "A senha deve ter entre 8 e 64 caracteres.");
        require(password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
            "A senha contém muitos caracteres multibyte. Use até 72 bytes em UTF-8.");
        verifier.verify(ra, invitation);
        require(!accounts.existsByRa(ra), "Este R.A. já possui cadastro.");
        return accounts.save(new AppUser(ra, name, course, passwords.hash(password)));
    }
}
