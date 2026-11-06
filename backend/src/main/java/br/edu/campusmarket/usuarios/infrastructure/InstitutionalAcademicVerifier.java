package br.edu.campusmarket.usuarios.infrastructure;

import br.edu.campusmarket.common.BusinessException;
import br.edu.campusmarket.usuarios.domain.AcademicVerifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Falha de forma explícita até existir integração autorizada pela instituição. */
@Component
@Profile("postgres & !demo & !test")
public class InstitutionalAcademicVerifier implements AcademicVerifier {
    public void verify(String ra, String invitation) {
        throw new BusinessException(503,
            "Cadastro institucional ainda não configurado. Solicite a integração à faculdade.");
    }
}
