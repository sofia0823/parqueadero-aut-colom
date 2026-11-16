package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.CuentaPersonal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCuentaPersonal
        extends JpaRepository<CuentaPersonal, Long> {

    Optional<CuentaPersonal> findByNombreUsuarioIgnoreCase(String nombreUsuario);
}
