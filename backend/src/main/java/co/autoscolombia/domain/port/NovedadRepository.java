package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.Novedad;
import java.util.List;
import java.util.Optional;

public interface NovedadRepository {

    List<Novedad> listar();

    Optional<Novedad> buscarPorId(Long id);

    Novedad guardar(Novedad novedad);
}
