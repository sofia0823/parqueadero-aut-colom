
package co.autoscolombia.application;

import co.autoscolombia.application.dto.ConfirmarPagoRequest;
import co.autoscolombia.application.dto.LiquidacionEstancia;
import co.autoscolombia.domain.model.Celda;
import co.autoscolombia.domain.model.Estancia;
import co.autoscolombia.domain.model.Pago;
import co.autoscolombia.domain.model.Vehiculo;
import co.autoscolombia.domain.port.CeldaRepository;
import co.autoscolombia.domain.port.EstanciaRepository;
import co.autoscolombia.domain.port.PagoRepository;
import co.autoscolombia.domain.port.VehiculoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EstanciaService {

    private static final BigDecimal TARIFA_POR_HORA =
            new BigDecimal("2000.00");

    private final EstanciaRepository estancias;
    private final VehiculoRepository vehiculos;
    private final CeldaRepository celdas;
    private final PagoRepository pagos;

    public EstanciaService(
            EstanciaRepository estancias,
            VehiculoRepository vehiculos,
            CeldaRepository celdas,
            PagoRepository pagos) {

        this.estancias = estancias;
        this.vehiculos = vehiculos;
        this.celdas = celdas;
        this.pagos = pagos;
    }

    /**
     * Lista todas las estancias registradas.
     */
    @Transactional(readOnly = true)
    public List<Estancia> listar() {
        return estancias.listar();
    }

    /**
     * Lista las estancias activas.
     */
    @Transactional(readOnly = true)
    public List<Estancia> listarActivas() {
        return estancias.listarActivas();
    }

    /**
     * Obtiene las celdas disponibles.
     */
    @Transactional(readOnly = true)
    public List<Celda> listarCeldasDisponibles() {
        return celdas.listarDisponibles();
    }

    /**
     * Registra la entrada de un vehículo al parqueadero.
     */
    @Transactional
    public Estancia registrarEntrada(
            Long vehiculoId,
            Long celdaId,
            Long registradoPor) {

        if (vehiculoId == null || celdaId == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un vehículo y una celda."
            );
        }

        Vehiculo vehiculo = vehiculos.buscarPorId(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El vehículo no existe."
                ));

        if (estancias.existeActivaPorVehiculo(vehiculoId)) {
            throw new IllegalArgumentException(
                    "Este vehículo ya tiene una entrada registrada sin salida."
            );
        }

        Celda celda = celdas.buscarPorId(celdaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "La celda no existe."
                ));

        if (celda.getEstado() != Celda.Estado.DISPONIBLE) {
            throw new IllegalArgumentException(
                    "La celda seleccionada no está disponible."
            );
        }

        Estancia estancia = new Estancia(
                vehiculo,
                celda,
                registradoPor
        );

        celda.setEstado(Celda.Estado.OCUPADA);
        celdas.guardar(celda);

        return estancias.guardar(estancia);
    }

    /**
     * Calcula el valor de una estancia activa.
     *
     * Tarifa: $2.000 COP por hora iniciada.
     * Se cobra como mínimo una hora.
     */
    @Transactional(readOnly = true)
    public LiquidacionEstancia calcularLiquidacion(
            Long estanciaId) {

        Estancia estancia = buscarEstanciaActiva(estanciaId);

        LocalDateTime ahora = LocalDateTime.now();

        long segundos = Math.max(
                0L,
                Duration.between(
                        estancia.getEntrada(),
                        ahora
                ).getSeconds()
        );

        long minutos = (segundos + 59L) / 60L;

        long horasFacturadas = Math.max(
                1L,
                (segundos + 3599L) / 3600L
        );

        BigDecimal total = TARIFA_POR_HORA.multiply(
                BigDecimal.valueOf(horasFacturadas)
        );

        return new LiquidacionEstancia(
                estancia.getId(),
                estancia.getVehiculo().getPlaca(),
                estancia.getEntrada(),
                ahora,
                minutos,
                horasFacturadas,
                TARIFA_POR_HORA,
                total
        );
    }

    /**
     * Registra el pago y la salida en una transacción.
     */
    @Transactional
    public Pago confirmarPagoYRegistrarSalida(
            Long estanciaId,
            ConfirmarPagoRequest solicitud) {

        if (solicitud == null
                || solicitud.metodo() == null
                || solicitud.metodo().isBlank()) {

            throw new IllegalArgumentException(
                    "Debes seleccionar un método de pago."
            );
        }

        String metodo = solicitud.metodo()
                .trim()
                .toUpperCase(Locale.ROOT);

        List<String> metodosPermitidos = List.of(
                "EFECTIVO",
                "TARJETA",
                "TRANSFERENCIA"
        );

        if (!metodosPermitidos.contains(metodo)) {
            throw new IllegalArgumentException(
                    "Método de pago no válido."
            );
        }

        Estancia estancia = buscarEstanciaActiva(estanciaId);

        if (pagos.existePorEstancia(estanciaId)) {
            throw new IllegalArgumentException(
                    "Esta estancia ya tiene un pago registrado."
            );
        }

        /*
         * El importe se calcula en el backend.
         * No se acepta un total enviado por el frontend.
         */
        LiquidacionEstancia liquidacion =
                calcularLiquidacion(estanciaId);

        Pago pago = new Pago(
                estancia,
                liquidacion.total(),
                metodo
        );

        Pago pagoGuardado = pagos.guardar(pago);

        Celda celda = estancia.getCelda();

        estancia.setSalida(LocalDateTime.now());
        celda.setEstado(Celda.Estado.DISPONIBLE);

        celdas.guardar(celda);
        estancias.guardar(estancia);

        return pagoGuardado;
    }

    /**
     * Busca una estancia activa por su identificador.
     */
    private Estancia buscarEstanciaActiva(Long estanciaId) {

        if (estanciaId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la estancia es obligatorio."
            );
        }

        Estancia estancia = estancias.buscarPorId(estanciaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la estancia."
                ));

        if (estancia.getSalida() != null) {
            throw new IllegalArgumentException(
                    "La salida de este vehículo ya fue registrada."
            );
        }

        return estancia;
    }

    /**
     * Conserva el endpoint antiguo, pero bloquea las salidas
     * que no pasen por el proceso de pago.
     */
    @Transactional
    public Estancia registrarSalida(Long estanciaId) {

        throw new IllegalArgumentException(
                "Para registrar la salida debes confirmar primero el pago."
        );
    }
}
