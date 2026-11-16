package co.autoscolombia.infrastructure.persistence;

import co.autoscolombia.domain.model.Vehiculo;
import co.autoscolombia.domain.port.VehiculoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

interface SpringDataVehiculo extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlacaIgnoreCase(String placa);
}

@Component
public class JpaVehiculoRepository implements VehiculoRepository {
    private final SpringDataVehiculo jpa;
    public JpaVehiculoRepository(SpringDataVehiculo jpa) { this.jpa = jpa; }
    @Override public List<Vehiculo> listar() { return jpa.findAll(); }
    @Override public Optional<Vehiculo> buscarPorId(Long id) { return jpa.findById(id); }
    @Override public boolean existePlaca(String placa) { return jpa.existsByPlacaIgnoreCase(placa); }
    @Override public Vehiculo guardar(Vehiculo vehiculo) { return jpa.save(vehiculo); }
}
