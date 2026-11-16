package co.autoscolombia.application;

import co.autoscolombia.domain.model.Vehiculo;
import co.autoscolombia.domain.port.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VehiculoService {
    private final VehiculoRepository repository;
    public VehiculoService(VehiculoRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Vehiculo> listar() { return repository.listar(); }

    @Transactional
    public Vehiculo registrar(Vehiculo vehiculo) {
        if (vehiculo.getPlaca() == null || vehiculo.getPlaca().isBlank())
            throw new IllegalArgumentException("La placa es obligatoria.");
        vehiculo.setPlaca(vehiculo.getPlaca().trim().toUpperCase());
        if (repository.existePlaca(vehiculo.getPlaca()))
            throw new IllegalArgumentException("Ya existe un vehículo registrado con esa placa.");
        if (vehiculo.getPropietario() == null || vehiculo.getPropietario().isBlank())
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        return repository.guardar(vehiculo);
    }
}
