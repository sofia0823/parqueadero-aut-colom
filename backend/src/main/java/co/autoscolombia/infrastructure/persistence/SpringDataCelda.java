
package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Celda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataCelda extends JpaRepository<Celda, Long> {

    List<Celda> findByEstado(Celda.Estado estado);

    boolean existsByCodigoIgnoreCase(String codigo);
}
