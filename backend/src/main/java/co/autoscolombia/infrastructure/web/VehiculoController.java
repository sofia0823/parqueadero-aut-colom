package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.VehiculoService;
import co.autoscolombia.domain.model.Vehiculo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = {"http://localhost:5173"})
public class VehiculoController {
    private final VehiculoService service;
    public VehiculoController(VehiculoService service) { this.service = service; }

    @GetMapping public List<Vehiculo> listar() { return service.listar(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehiculo registrar(@Valid @RequestBody Vehiculo vehiculo) {
        return service.registrar(vehiculo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarError(IllegalArgumentException ex) {
        return Map.of("mensaje", ex.getMessage());
    }
}
