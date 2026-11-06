package br.edu.campusmarket.domain;
import br.edu.campusmarket.usuarios.domain.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/** Mesmo contrato observado em duas implementações substituíveis (LSP). */
@SpringBootTest @ActiveProfiles("test")
class UserAccountsContractTest {
    @Autowired UserAccounts database;
    private static final AtomicInteger sequence = new AtomicInteger(70000000);
    private void verifyContract(UserAccounts accounts) {
        String ra = String.valueOf(sequence.incrementAndGet());
        assertFalse(accounts.existsByRa(ra)); assertTrue(accounts.findByRa(ra).isEmpty());
        var user = new AppUser(ra, "Estudante contrato", "Computação", "hash");
        assertEquals(user.id(), accounts.save(user).id());
        assertTrue(accounts.existsByRa(ra));
        var restored = accounts.findByRa(ra).orElseThrow();
        assertEquals(user.id(), restored.id()); assertEquals(ra, restored.ra());
    }
    @Test void memoryImplementationPreservesContract() { verifyContract(new RegisterUserTest.MemoryAccounts()); }
    @Test void jpaImplementationPreservesContract() { verifyContract(database); }
}
