
package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Estancia;
import java.util.List;
import java.util.Optional;

public interface EstanciaRepository {
    List<Estancia> listar();
    List<Estancia> listarActivas();
    Optional<Estancia> buscarPorId(Long id);
    boolean existeActivaPorVehiculo(Long vehiculoId);
    Estancia guardar(Estancia estancia);
}
