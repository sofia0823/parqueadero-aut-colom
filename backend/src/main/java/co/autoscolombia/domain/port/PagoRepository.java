package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Pago;
import java.util.Optional;

public interface PagoRepository {

    boolean existePorEstancia(Long estanciaId);

    Optional<Pago> buscarPorEstancia(Long estanciaId);

    Pago guardar(Pago pago);
}