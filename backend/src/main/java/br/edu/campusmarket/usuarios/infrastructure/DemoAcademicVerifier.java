package br.edu.campusmarket.usuarios.infrastructure;

import br.edu.campusmarket.usuarios.domain.AcademicVerifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import static br.edu.campusmarket.common.BusinessException.require;

/** Apenas demonstração: não consulta nem comprova vínculo com faculdade real. */
@Component
@Profile({"demo", "test"})
public class DemoAcademicVerifier implements AcademicVerifier {
    public void verify(String ra, String invitation) {
        require(ra != null && ra.matches("202600(?:0[1-9]|[1-4][0-9]|50)"),
            "R.A. fora da base acadêmica fictícia (20260001 a 20260050).");
        require(("CAMPUS-" + ra.substring(4)).equals(invitation),
            "Código de convite acadêmico inválido.");
    }
}
