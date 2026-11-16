package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.NovedadService;
import co.autoscolombia.application.dto.NovedadResponse;
import co.autoscolombia.application.dto.RegistrarNovedadRequest;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/novedades")
public class NovedadController {

    private final NovedadService service;

    public NovedadController(NovedadService service) {
        this.service = service;
    }

    @GetMapping
    public List<NovedadResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public NovedadResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NovedadResponse registrar(
            @RequestBody RegistrarNovedadRequest solicitud,
            Principal principal) {
        return service.registrar(solicitud, principal);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarValidacion(
            IllegalArgumentException ex) {
        return Map.of("mensaje", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>>
            manejarEstado(ResponseStatusException ex) {
        return org.springframework.http.ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("mensaje", ex.getReason()));
    }
}
