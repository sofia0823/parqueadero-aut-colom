package co.autoscolombia.infrastructure.security;

import co.autoscolombia.infrastructure.persistence.SpringDataCuentaPersonal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoUserDetailsService implements UserDetailsService {

    private final SpringDataCuentaPersonal cuentas;

    public EmpleadoUserDetailsService(SpringDataCuentaPersonal cuentas) {
        this.cuentas = cuentas;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var cuenta = cuentas.findByNombreUsuarioIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciales no válidas."
                ));

        return User.withUsername(cuenta.getNombreUsuario())
                .password(cuenta.getPasswordHash())
                .authorities("ROLE_" + cuenta.getRol().name())
                .disabled(!cuenta.isActivo())
                .build();
    }
}
