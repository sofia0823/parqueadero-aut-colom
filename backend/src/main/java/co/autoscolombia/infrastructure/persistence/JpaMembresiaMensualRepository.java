package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.MembresiaMensual;
import co.autoscolombia.domain.port.MembresiaMensualRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaMembresiaMensualRepository
        implements MembresiaMensualRepository {

    private final SpringDataMembresiaMensual jpa;

    public JpaMembresiaMensualRepository(SpringDataMembresiaMensual jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<MembresiaMensual> listar() {
        return jpa.findAllByOrderByFechaInicioDesc();
    }

    @Override
    public List<MembresiaMensual> listarPorUsuario(Long usuarioId) {
        return jpa.findAllByUsuario_Id(usuarioId);
    }

    @Override
    public Optional<MembresiaMensual> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public MembresiaMensual guardar(MembresiaMensual membresia) {
        return jpa.save(membresia);
    }
}
