
package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Estancia;
import co.autoscolombia.domain.port.EstanciaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

interface SpringDataEstancia extends JpaRepository<Estancia, Long> {

    List<Estancia> findAllByOrderByEntradaDesc();

    List<Estancia> findBySalidaIsNullOrderByEntradaDesc();

    boolean existsByVehiculo_IdAndSalidaIsNull(Long vehiculoId);
}

@Component
public class JpaEstanciaRepository implements EstanciaRepository {

    private final SpringDataEstancia jpa;

    public JpaEstanciaRepository(SpringDataEstancia jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Estancia> listar() {
        return jpa.findAllByOrderByEntradaDesc();
    }

    @Override
    public List<Estancia> listarActivas() {
        return jpa.findBySalidaIsNullOrderByEntradaDesc();
    }

    @Override
    public Optional<Estancia> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existeActivaPorVehiculo(Long vehiculoId) {
        return jpa.existsByVehiculo_IdAndSalidaIsNull(vehiculoId);
    }

    @Override
    public Estancia guardar(Estancia estancia) {
        return jpa.save(estancia);
    }
}
