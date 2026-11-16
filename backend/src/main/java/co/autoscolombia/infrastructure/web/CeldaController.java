
package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.CeldaService;
import co.autoscolombia.domain.model.Celda;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/celdas")
@CrossOrigin(origins = "http://localhost:5173")
public class CeldaController {

    private final CeldaService celdaService;

    public CeldaController(CeldaService celdaService) {
        this.celdaService = celdaService;
    }

    // Consultar todas las celdas
    @GetMapping
    public ResponseEntity<List<Celda>> listar() {
        return ResponseEntity.ok(celdaService.listar());
    }

    // Consultar celdas disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<Celda>> listarDisponibles() {
        return ResponseEntity.ok(celdaService.listarDisponibles());
    }

    // Registrar una celda
    @PostMapping
    public ResponseEntity<?> registrar(
            @RequestBody CeldaRequest request) {

        if (request == null
                || request.getCodigo() == null
                || request.getTipo() == null) {

            Map<String, String> error = new HashMap<>();
            error.put(
                "mensaje",
                "El código y el tipo son obligatorios."
            );

            return ResponseEntity.badRequest().body(error);
        }

        Celda celda = celdaService.registrar(
            request.getCodigo(),
            request.getTipo()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(celda);
    }

    // Inicializar las 20 celdas si todavía no existen
    @PostMapping("/inicializar")
    public ResponseEntity<List<Celda>> crearVeinte() {
        List<Celda> creadas = celdaService.crearVeinte();

        return ResponseEntity.status(HttpStatus.CREATED).body(creadas);
    }

    // Manejar errores de validación
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    // DTO para recibir los datos desde React
    public static class CeldaRequest {

        private String codigo;
        private String tipo;

        public CeldaRequest() {
        }

        public String getCodigo() {
            return codigo;
        }

        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }
}
