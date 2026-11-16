package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.MensualidadService;
import co.autoscolombia.application.ConflictoMensualidadException;
import co.autoscolombia.application.dto.EstadoMensualidadResponse;
import co.autoscolombia.application.dto.MembresiaMensualResponse;
import co.autoscolombia.application.dto.PagoMensualResponse;
import co.autoscolombia.application.dto.RegistrarMembresiaRequest;
import co.autoscolombia.application.dto.RegistrarPagoMensualRequest;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mensualidades")
public class MensualidadController {

    private final MensualidadService service;

    public MensualidadController(MensualidadService service) {
        this.service = service;
    }

    @GetMapping("/membresias")
    public List<MembresiaMensualResponse> listarMembresias() {
        return service.listarMembresias();
    }

    @PostMapping("/membresias")
    public ResponseEntity<MembresiaMensualResponse> registrarMembresia(
            @RequestBody RegistrarMembresiaRequest solicitud) {
        var resultado = service.registrarMembresia(solicitud);
        return ResponseEntity
                .status(resultado.repetida()
                        ? HttpStatus.OK
                        : HttpStatus.CREATED)
                .body(resultado.resultado());
    }

    @PostMapping("/membresias/{id}/finalizar")
    public MembresiaMensualResponse finalizarMembresia(
            @PathVariable("id") Long id) {
        return service.finalizarMembresia(id);
    }

    @GetMapping("/estado")
    public List<EstadoMensualidadResponse> consultarEstado(
            @RequestParam(name = "periodo", required = false) String periodo) {
        return service.consultarEstado(periodo);
    }

    @GetMapping("/pagos")
    public List<PagoMensualResponse> listarPagos(
            @RequestParam(name = "periodo", required = false) String periodo) {
        return service.listarPagos(periodo);
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoMensualResponse> registrarPago(
            @RequestBody RegistrarPagoMensualRequest solicitud) {
        var resultado = service.registrarPago(solicitud);
        return ResponseEntity
                .status(resultado.repetida()
                        ? HttpStatus.OK
                        : HttpStatus.CREATED)
                .body(resultado.resultado());
    }

    @ExceptionHandler(ConflictoMensualidadException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> manejarConflicto(
            ConflictoMensualidadException ex) {
        return Map.of("mensaje", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarError(IllegalArgumentException ex) {
        return Map.of("mensaje", ex.getMessage());
    }
}
