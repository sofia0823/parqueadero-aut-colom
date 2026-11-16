package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataPago extends JpaRepository<Pago, Long> {

    boolean existsByEstancia_Id(Long estanciaId);

    Optional<Pago> findByEstancia_Id(Long estanciaId);
}