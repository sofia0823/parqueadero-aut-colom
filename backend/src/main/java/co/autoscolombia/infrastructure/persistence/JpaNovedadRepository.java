package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Novedad;
import co.autoscolombia.domain.port.NovedadRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaNovedadRepository implements NovedadRepository {

    private final SpringDataNovedad jpa;

    public JpaNovedadRepository(SpringDataNovedad jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Novedad> listar() {
        return jpa.findAllByOrderByFechaHoraDesc();
    }

    @Override
    public Optional<Novedad> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Novedad guardar(Novedad novedad) {
        return jpa.save(novedad);
    }
}
