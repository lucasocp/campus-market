package br.edu.campusmarket.usuarios.api;

import br.edu.campusmarket.common.CurrentUser;
import br.edu.campusmarket.usuarios.application.RegisterUser;
import br.edu.campusmarket.usuarios.domain.AppUser;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authentication;
    private final SecurityContextRepository contexts;
    private final CurrentUser current;
    private final RegisterUser register;
    public AuthController(AuthenticationManager authentication, SecurityContextRepository contexts,
                          CurrentUser current, RegisterUser register) {
        this.authentication = authentication; this.contexts = contexts;
        this.current = current; this.register = register;
    }
    public record Login(@NotBlank String ra, @NotBlank @Size(max = 64) String password) { }
    public record Registration(@Pattern(regexp = "[0-9]{5,20}") @NotBlank String ra,
        @NotBlank @Size(max = 100) String name, @NotBlank @Size(max = 100) String course,
        @NotBlank @Size(min = 8, max = 64) String password, @NotBlank String invitation) { }
    public record Me(UUID id, String ra, String name, String course, String biography) {
        public static Me of(AppUser u) { return new Me(u.id(), u.ra(), u.name(), u.course(), u.biography()); }
    }
    @GetMapping("/csrf") Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
    @PostMapping("/login") Me login(@Valid @RequestBody Login input, HttpServletRequest request, HttpServletResponse response) {
        var result = authentication.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(input.ra(), input.password()));
        new ChangeSessionIdAuthenticationStrategy().onAuthentication(result, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(result);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        return Me.of(current.get());
    }
    @PostMapping("/register") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    Me register(@Valid @RequestBody Registration input) {
        return Me.of(register.execute(input.ra(), input.name(), input.course(), input.password(), input.invitation()));
    }
    @GetMapping("/me") Me me() { return Me.of(current.get()); }
    @PostMapping("/logout") void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }
}
