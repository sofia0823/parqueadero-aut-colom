package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.MembresiaMensual;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataMembresiaMensual
        extends JpaRepository<MembresiaMensual, Long> {

    @EntityGraph(attributePaths = "usuario")
    List<MembresiaMensual> findAllByOrderByFechaInicioDesc();

    @EntityGraph(attributePaths = "usuario")
    List<MembresiaMensual> findAllByUsuario_Id(Long usuarioId);
}
