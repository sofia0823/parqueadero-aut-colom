
package co.autoscolombia.infrastructure.web;

import co.autoscolombia.application.EstanciaService;
import co.autoscolombia.application.dto.ConfirmarPagoRequest;
import co.autoscolombia.application.dto.LiquidacionEstancia;
import co.autoscolombia.domain.model.Celda;
import co.autoscolombia.domain.model.Estancia;
import co.autoscolombia.domain.model.Pago;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estancias")
@CrossOrigin(origins = "http://localhost:5173")
public class EstanciaController {

    private final EstanciaService service;

    public EstanciaController(EstanciaService service) {
        this.service = service;
    }

    public static class EntradaRequest {

        private Long vehiculoId;
        private Long celdaId;
        private Long registradoPor;

        public Long getVehiculoId() {
            return vehiculoId;
        }

        public void setVehiculoId(Long vehiculoId) {
            this.vehiculoId = vehiculoId;
        }

        public Long getCeldaId() {
            return celdaId;
        }

        public void setCeldaId(Long celdaId) {
            this.celdaId = celdaId;
        }

        public Long getRegistradoPor() {
            return registradoPor;
        }

        public void setRegistradoPor(Long registradoPor) {
            this.registradoPor = registradoPor;
        }
    }

    // Listar todas las estancias
    @GetMapping
    public List<Estancia> listar() {
        return service.listar();
    }

    // Listar estancias activas
    @GetMapping("/activas")
    public List<Estancia> listarActivas() {
        return service.listarActivas();
    }

    // Listar celdas disponibles
    @GetMapping("/celdas-disponibles")
    public List<Celda> listarCeldasDisponibles() {
        return service.listarCeldasDisponibles();
    }

    // Registrar entrada de un vehículo
    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public Estancia registrarEntrada(
            @RequestBody EntradaRequest request) {

        if (request == null
                || request.getVehiculoId() == null
                || request.getCeldaId() == null) {
            throw new IllegalArgumentException(
                    "Debes indicar el vehículo y la celda."
            );
        }

        return service.registrarEntrada(
                request.getVehiculoId(),
                request.getCeldaId(),
                request.getRegistradoPor()
        );
    }

    // Calcular liquidación de una estancia
    @GetMapping("/{id}/liquidacion")
    public LiquidacionEstancia calcularLiquidacion(
            @PathVariable("id") Long id) {

        return service.calcularLiquidacion(id);
    }

    // Confirmar pago y registrar salida
    @PostMapping("/{id}/pago-salida")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> confirmarPagoYSalida(
            @PathVariable("id") Long id,
            @RequestBody ConfirmarPagoRequest solicitud) {

        if (solicitud == null || solicitud.metodo() == null
                || solicitud.metodo().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un método de pago."
            );
        }

        Pago pago = service.confirmarPagoYRegistrarSalida(
                id,
                solicitud
        );

        return Map.of(
                "mensaje", "Pago registrado y salida confirmada.",
                "pagoId", pago.getId(),
                "estanciaId", id,
                "valor", pago.getValor(),
                "metodo", pago.getMetodo(),
                "estado", pago.getEstado().name(),
                "fechaPago", pago.getFechaPago()
        );
    }

    // Evitar salida sin pago
    @PostMapping("/{id}/salida")
    public Estancia registrarSalida(@PathVariable("id") Long id) {
        return service.registrarSalida(id);
    }

    // Manejar errores de validación
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> manejarError(
            IllegalArgumentException ex) {

        return Map.of(
                "mensaje",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "No fue posible completar la operación."
        );
    }
}
