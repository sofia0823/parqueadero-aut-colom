
package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Celda;
import co.autoscolombia.domain.port.CeldaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCeldaRepository implements CeldaRepository {

    private final SpringDataCelda jpa;

    public JpaCeldaRepository(SpringDataCelda jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Celda> listar() {
        return jpa.findAll();
    }

    @Override
    public Optional<Celda> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Celda> listarDisponibles() {
        return jpa.findByEstado(Celda.Estado.DISPONIBLE);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return jpa.existsByCodigoIgnoreCase(codigo);
    }

    @Override
    public Celda guardar(Celda celda) {
        return jpa.save(celda);
    }
}
