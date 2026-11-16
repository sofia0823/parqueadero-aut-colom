package co.autoscolombia.application;

import co.autoscolombia.application.dto.NovedadResponse;
import co.autoscolombia.application.dto.RegistrarNovedadRequest;
import co.autoscolombia.domain.model.Estancia;
import co.autoscolombia.domain.model.Novedad;
import co.autoscolombia.domain.model.Vehiculo;
import co.autoscolombia.domain.port.EstanciaRepository;
import co.autoscolombia.domain.port.NovedadRepository;
import co.autoscolombia.domain.port.VehiculoRepository;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NovedadService {

    private final NovedadRepository novedades;
    private final VehiculoRepository vehiculos;
    private final EstanciaRepository estancias;

    public NovedadService(
            NovedadRepository novedades,
            VehiculoRepository vehiculos,
            EstanciaRepository estancias) {
        this.novedades = novedades;
        this.vehiculos = vehiculos;
        this.estancias = estancias;
    }

    @Transactional(readOnly = true)
    public List<NovedadResponse> listar() {
        return novedades.listar().stream().map(NovedadService::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public NovedadResponse buscarPorId(Long id) {
        return respuesta(novedades.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La novedad solicitada no existe."
                )));
    }

    @Transactional
    public NovedadResponse registrar(
            RegistrarNovedadRequest solicitud,
            Principal principal) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                    "Debes enviar los datos de la novedad."
            );
        }
        boolean tieneVehiculo = solicitud.vehiculoId() != null;
        boolean tieneEstancia = solicitud.estanciaId() != null;
        if (tieneVehiculo == tieneEstancia) {
            throw new IllegalArgumentException(
                    "Debes asociar la novedad a un vehículo o a una estancia, no a ambos."
            );
        }
        if (solicitud.descripcion() == null
                || solicitud.descripcion().isBlank()) {
            throw new IllegalArgumentException(
                    "La descripción de la novedad es obligatoria."
            );
        }
        String descripcion = solicitud.descripcion().trim();
        if (descripcion.length() > 1000) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar 1000 caracteres."
            );
        }
        if (principal == null
                || principal.getName() == null
                || principal.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "No fue posible identificar al responsable autenticado."
            );
        }

        Vehiculo vehiculo = null;
        Estancia estancia = null;
        if (tieneVehiculo) {
            vehiculo = vehiculos.buscarPorId(solicitud.vehiculoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El vehículo indicado no existe."
                    ));
        } else {
            estancia = estancias.buscarPorId(solicitud.estanciaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "La estancia indicada no existe."
                    ));
        }

        return respuesta(novedades.guardar(new Novedad(
                vehiculo,
                estancia,
                descripcion,
                principal.getName()
        )));
    }

    private static NovedadResponse respuesta(Novedad novedad) {
        return new NovedadResponse(
                novedad.getId(),
                novedad.getVehiculo() == null
                        ? null
                        : novedad.getVehiculo().getId(),
                novedad.getVehiculo() == null
                        ? null
                        : novedad.getVehiculo().getPlaca(),
                novedad.getEstancia() == null
                        ? null
                        : novedad.getEstancia().getId(),
                novedad.getDescripcion(),
                novedad.getFechaHora(),
                novedad.getResponsable()
        );
    }
}
