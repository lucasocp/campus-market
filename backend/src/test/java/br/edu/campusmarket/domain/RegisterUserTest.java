package br.edu.campusmarket.domain;
import br.edu.campusmarket.common.BusinessException;
import br.edu.campusmarket.usuarios.application.RegisterUser;
import br.edu.campusmarket.usuarios.domain.*;
import br.edu.campusmarket.usuarios.infrastructure.DemoAcademicVerifier;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Demonstra DIP: usa repositório em memória e hasher controlado, sem Spring. */
class RegisterUserTest {
    static class MemoryAccounts implements UserAccounts {
        private final Map<String, AppUser> data = new HashMap<>();
        public boolean existsByRa(String ra) { return data.containsKey(ra); }
        public AppUser save(AppUser user) { data.put(user.ra(), user); return user; }
        public Optional<AppUser> findByRa(String ra) { return Optional.ofNullable(data.get(ra)); }
    }
    @Test void validStudentIsStoredWithHashInsteadOfRawPassword() {
        var accounts = new MemoryAccounts();
        var service = new RegisterUser(new DemoAcademicVerifier(), accounts, raw -> "hashed:" + raw);
        var user = service.execute("20260004", "Ana Silva", "Direito", "Senha@123", "CAMPUS-0004");
        assertEquals("hashed:Senha@123", user.passwordHash());
        assertEquals(user.id(), accounts.findByRa("20260004").orElseThrow().id());
    }
    @Test void knownRaWithoutCorrectInvitationIsRejected() {
        var service = new RegisterUser(new DemoAcademicVerifier(), new MemoryAccounts(), raw -> "hash");
        assertThrows(BusinessException.class, () -> service.execute("20260004", "Ana", "Direito", "Senha@123", "CAMPUS-0005"));
    }
    @Test void duplicateRaIsRejected() {
        var service = new RegisterUser(new DemoAcademicVerifier(), new MemoryAccounts(), raw -> "hash");
        service.execute("20260004", "Ana", "Direito", "Senha@123", "CAMPUS-0004");
        assertThrows(BusinessException.class, () -> service.execute("20260004", "Ana", "Direito", "Senha@123", "CAMPUS-0004"));
    }
    @Test void alternativeVerifierCanBeInjectedWithoutChangingRegistration() {
        AcademicVerifier authorizedSource = (ra, code) -> { if (!"institution-token".equals(code)) throw new BusinessException("Não confirmado."); };
        var service = new RegisterUser(authorizedSource, new MemoryAccounts(), raw -> "hash");
        assertDoesNotThrow(() -> service.execute("90000001", "Ana", "Direito", "Senha@123", "institution-token"));
    }
    @Test void unknownRaIsRejectedByDemoSource() {
        assertThrows(BusinessException.class, () -> new DemoAcademicVerifier().verify("99999999", "CAMPUS-9999"));
    }
}
