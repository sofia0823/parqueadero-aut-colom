package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.dto.AuthSessionResponse;
import co.autoscolombia.application.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return Map.of("headerName", csrfToken.getHeaderName());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        if (request == null
                || !StringUtils.hasText(request.username())
                || !StringUtils.hasText(request.password())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "mensaje",
                    "Debes ingresar usuario y contraseña."
            ));
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(),
                            request.password()
                    )
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "mensaje",
                    "Usuario o contraseña incorrectos."
            ));
        }

        sessionAuthenticationStrategy.onAuthentication(
                authentication,
                servletRequest,
                servletResponse
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(
                context,
                servletRequest,
                servletResponse
        );

        return ResponseEntity.ok(respuestaSesion(authentication));
    }

    @GetMapping("/session")
    public AuthSessionResponse session(Authentication authentication) {
        return respuestaSesion(authentication);
    }

    private AuthSessionResponse respuestaSesion(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new AuthenticationServiceException(
                        "La cuenta autenticada no tiene un rol válido."
                ));
        return new AuthSessionResponse(authentication.getName(), role);
    }
}
