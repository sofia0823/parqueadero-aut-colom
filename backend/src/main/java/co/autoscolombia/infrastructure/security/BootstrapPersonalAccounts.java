package co.autoscolombia.infrastructure.security;

import co.autoscolombia.domain.model.CuentaPersonal;
import co.autoscolombia.domain.model.RolPersonal;
import co.autoscolombia.infrastructure.persistence.SpringDataCuentaPersonal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BootstrapPersonalAccounts implements ApplicationRunner {

    private static final int PASSWORD_MIN_LENGTH = 12;

    private final SpringDataCuentaPersonal cuentas;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String operatorUsername;
    private final String operatorPassword;

    public BootstrapPersonalAccounts(
            SpringDataCuentaPersonal cuentas,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.bootstrap-admin.username:}") String adminUsername,
            @Value("${app.security.bootstrap-admin.password:}") String adminPassword,
            @Value("${app.security.bootstrap-operator.username:}") String operatorUsername,
            @Value("${app.security.bootstrap-operator.password:}") String operatorPassword) {
        this.cuentas = cuentas;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.operatorUsername = operatorUsername;
        this.operatorPassword = operatorPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        validarCredenciales(adminUsername, adminPassword, "administrador");
        crearOActualizar(adminUsername, adminPassword, RolPersonal.ADMINISTRADOR);

        boolean operatorUsernameProvided = StringUtils.hasText(operatorUsername);
        boolean operatorPasswordProvided = StringUtils.hasText(operatorPassword);
        if (operatorUsernameProvided != operatorPasswordProvided) {
            throw new IllegalStateException(
                    "Debes configurar usuario y contraseña del operario juntos."
            );
        }
        if (operatorUsernameProvided) {
            validarCredenciales(operatorUsername, operatorPassword, "operario");
            if (adminUsername.trim().equalsIgnoreCase(operatorUsername.trim())) {
                throw new IllegalStateException(
                        "Administrador y operario deben usar cuentas distintas."
                );
            }
            crearOActualizar(
                    operatorUsername,
                    operatorPassword,
                    RolPersonal.OPERARIO
            );
        }
    }

    private void validarCredenciales(
            String username,
            String password,
            String label) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "Configura usuario y contraseña iniciales del " + label
                            + " mediante variables de entorno."
            );
        }
        if (username.trim().length() > 50
                || password.length() < PASSWORD_MIN_LENGTH) {
            throw new IllegalStateException(
                    "El usuario debe tener máximo 50 caracteres y la contraseña"
                            + " del " + label + " al menos 12."
            );
        }
    }

    private void crearOActualizar(
            String username,
            String password,
            RolPersonal role) {
        String normalizedUsername = username.trim();
        var existente = cuentas.findByNombreUsuarioIgnoreCase(normalizedUsername);
        if (existente.isPresent()) {
            CuentaPersonal cuenta = existente.get();
            if (cuenta.getRol() != role) {
                throw new IllegalStateException(
                        "La cuenta inicial ya existe con un rol diferente."
                );
            }
            if (!passwordEncoder.matches(password, cuenta.getPasswordHash())) {
                cuenta.actualizarPasswordHash(passwordEncoder.encode(password));
                cuentas.save(cuenta);
            }
            return;
        }

        cuentas.save(new CuentaPersonal(
                normalizedUsername,
                passwordEncoder.encode(password),
                role
        ));
    }
}
