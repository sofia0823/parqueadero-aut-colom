package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Vehiculo;
import java.util.List;
import java.util.Optional;

public interface VehiculoRepository {
    List<Vehiculo> listar();
    Optional<Vehiculo> buscarPorId(Long id);
    boolean existePlaca(String placa);
    Vehiculo guardar(Vehiculo vehiculo);
}
