package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Novedad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataNovedad extends JpaRepository<Novedad, Long> {

    List<Novedad> findAllByOrderByFechaHoraDesc();
}
