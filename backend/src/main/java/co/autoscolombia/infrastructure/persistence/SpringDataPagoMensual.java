package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.PagoMensual;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPagoMensual extends JpaRepository<PagoMensual, Long> {

    Optional<PagoMensual> findByUsuario_IdAndPeriodo(Long usuarioId, String periodo);

    @EntityGraph(attributePaths = "usuario")
    List<PagoMensual> findAllByPeriodoOrderByFechaPagoDesc(String periodo);

    @EntityGraph(attributePaths = "usuario")
    List<PagoMensual> findAllByOrderByFechaPagoDesc();
}
