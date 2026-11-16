
package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Celda;
import java.util.List;
import java.util.Optional;

public interface CeldaRepository {

    List<Celda> listar();

    Optional<Celda> buscarPorId(Long id);

    List<Celda> listarDisponibles();

    boolean existeCodigo(String codigo);

    Celda guardar(Celda celda);
}
