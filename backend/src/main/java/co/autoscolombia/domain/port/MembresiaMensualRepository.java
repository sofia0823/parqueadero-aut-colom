package co.autoscolombia.domain.port;

import co.autoscolombia.domain.model.MembresiaMensual;
import java.util.List;
import java.util.Optional;

public interface MembresiaMensualRepository {

    List<MembresiaMensual> listar();

    List<MembresiaMensual> listarPorUsuario(Long usuarioId);

    Optional<MembresiaMensual> buscarPorId(Long id);

    MembresiaMensual guardar(MembresiaMensual membresia);
}
