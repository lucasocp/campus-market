package br.edu.campusmarket.common;

import br.edu.campusmarket.usuarios.domain.AppUser;
import br.edu.campusmarket.usuarios.infrastructure.UserJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserJpaRepository users;
    public CurrentUser(UserJpaRepository users) { this.users = users; }
    public AppUser get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser"))
            throw new BusinessException(401, "Entre com seu R.A. para continuar.");
        return users.findByRa(auth.getName())
            .orElseThrow(() -> new BusinessException(401, "Sessão inválida. Entre novamente."));
    }
}
