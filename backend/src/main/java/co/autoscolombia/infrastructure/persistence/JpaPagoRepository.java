package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Pago;
import co.autoscolombia.domain.port.PagoRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaPagoRepository implements PagoRepository {

    private final SpringDataPago jpa;

    public JpaPagoRepository(SpringDataPago jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existePorEstancia(Long estanciaId) {
        return jpa.existsByEstancia_Id(estanciaId);
    }

    @Override
    public Optional<Pago> buscarPorEstancia(Long estanciaId) {
        return jpa.findByEstancia_Id(estanciaId);
    }

    @Override
    public Pago guardar(Pago pago) {
        return jpa.save(pago);
    }
}