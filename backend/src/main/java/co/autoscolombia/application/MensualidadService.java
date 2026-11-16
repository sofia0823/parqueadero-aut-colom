package co.autoscolombia.application;

import co.autoscolombia.application.dto.EstadoMensualidadResponse;
import co.autoscolombia.application.dto.MembresiaMensualResponse;
import co.autoscolombia.application.dto.PagoMensualResponse;
import co.autoscolombia.application.dto.RegistrarMembresiaRequest;
import co.autoscolombia.application.dto.RegistrarPagoMensualRequest;
import co.autoscolombia.application.dto.ResultadoIdempotente;
import co.autoscolombia.domain.model.MembresiaMensual;
import co.autoscolombia.domain.model.PagoMensual;
import co.autoscolombia.domain.model.Usuario;
import co.autoscolombia.domain.port.MembresiaMensualRepository;
import co.autoscolombia.domain.port.PagoMensualRepository;
import co.autoscolombia.domain.port.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MensualidadService {

    private static final Set<String> METODOS_PERMITIDOS = Set.of(
            "EFECTIVO",
            "TARJETA",
            "TRANSFERENCIA"
    );

    private final UsuarioRepository usuarios;
    private final MembresiaMensualRepository membresias;
    private final PagoMensualRepository pagos;

    public MensualidadService(
            UsuarioRepository usuarios,
            MembresiaMensualRepository membresias,
            PagoMensualRepository pagos) {
        this.usuarios = usuarios;
        this.membresias = membresias;
        this.pagos = pagos;
    }

    @Transactional(readOnly = true)
    public List<MembresiaMensualResponse> listarMembresias() {
        return membresias.listar().stream()
                .map(this::convertirMembresia)
                .toList();
    }

    @Transactional
    public ResultadoIdempotente<MembresiaMensualResponse> registrarMembresia(
            RegistrarMembresiaRequest solicitud) {
        if (solicitud == null
                || solicitud.usuarioId() == null
                || solicitud.fechaInicio() == null) {
            throw new IllegalArgumentException(
                    "Debes indicar el usuario y la fecha de inicio."
            );
        }

        if (solicitud.fechaInicio().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede estar en el futuro."
            );
        }

        Usuario usuario = usuarios.buscarPorIdConBloqueo(solicitud.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario indicado no existe."
                ));

        List<MembresiaMensual> membresiasUsuario =
                membresias.listarPorUsuario(solicitud.usuarioId());
        var existente = membresiasUsuario.stream()
                .filter(m -> m.getFechaInicio().equals(solicitud.fechaInicio()))
                .findFirst();
        if (existente.isPresent()) {
            return new ResultadoIdempotente<>(
                    convertirMembresia(existente.get()),
                    true
            );
        }

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException(
                    "No se puede inscribir un usuario inactivo."
            );
        }

        boolean tieneMembresiaVigente = membresiasUsuario.stream()
                .anyMatch(m -> m.getEstado() == MembresiaMensual.Estado.ACTIVA);

        if (tieneMembresiaVigente) {
            throw new ConflictoMensualidadException(
                    "El usuario ya tiene una membresía mensual vigente."
            );
        }

        MembresiaMensual membresia = membresias.guardar(
                new MembresiaMensual(usuario, solicitud.fechaInicio())
        );
        return new ResultadoIdempotente<>(
                convertirMembresia(membresia),
                false
        );
    }

    @Transactional
    public MembresiaMensualResponse finalizarMembresia(Long membresiaId) {
        if (membresiaId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la membresía es obligatorio."
            );
        }

        MembresiaMensual membresia = membresias.buscarPorId(membresiaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la membresía mensual."
                ));

        if (membresia.getEstado() != MembresiaMensual.Estado.ACTIVA) {
            throw new IllegalArgumentException(
                    "La membresía ya fue finalizada."
            );
        }

        membresia.finalizar(LocalDate.now());
        return convertirMembresia(membresias.guardar(membresia));
    }

    @Transactional
    public ResultadoIdempotente<PagoMensualResponse> registrarPago(
            RegistrarPagoMensualRequest solicitud) {
        if (solicitud == null
                || solicitud.usuarioId() == null
                || solicitud.valor() == null
                || solicitud.metodo() == null
                || solicitud.metodo().isBlank()) {
            throw new IllegalArgumentException(
                    "Debes indicar el usuario, período, valor y método de pago."
            );
        }

        YearMonth periodo = validarPeriodo(solicitud.periodo());
        BigDecimal valor = validarValor(solicitud.valor());
        String metodo = solicitud.metodo().trim().toUpperCase(Locale.ROOT);

        if (!METODOS_PERMITIDOS.contains(metodo)) {
            throw new IllegalArgumentException(
                    "Método de pago no válido."
            );
        }

        Usuario usuario = usuarios.buscarPorIdConBloqueo(solicitud.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario indicado no existe."
                ));

        var pagoExistente = pagos.buscarPorUsuarioYPeriodo(
                solicitud.usuarioId(),
                periodo.toString()
        );

        if (pagoExistente.isPresent()) {
            PagoMensual existente = pagoExistente.get();
            if (existente.getEstado() == PagoMensual.Estado.PAGADO
                    && existente.getValor().compareTo(valor) == 0
                    && existente.getMetodo().equals(metodo)) {
                return new ResultadoIdempotente<>(
                        convertirPago(existente),
                        true
                );
            }
            throw new ConflictoMensualidadException(
                    "Ya existe un pago distinto para ese usuario y período."
            );
        }

        boolean cubiertoPorMembresia = membresias
                .listarPorUsuario(solicitud.usuarioId())
                .stream()
                .anyMatch(m -> m.cubrePeriodo(periodo));

        if (!cubiertoPorMembresia) {
            throw new IllegalArgumentException(
                    "El usuario no tuvo una membresía mensual durante ese período."
            );
        }

        PagoMensual pago = pagos.guardar(new PagoMensual(
                usuario,
                periodo.toString(),
                valor,
                metodo
        ));
        return new ResultadoIdempotente<>(convertirPago(pago), false);
    }

    @Transactional(readOnly = true)
    public List<EstadoMensualidadResponse> consultarEstado(String periodoSolicitado) {
        YearMonth periodo = periodoSolicitado == null || periodoSolicitado.isBlank()
                ? YearMonth.now()
                : validarPeriodo(periodoSolicitado);

        Map<Long, MembresiaMensual> usuariosConMembresia = new LinkedHashMap<>();
        for (MembresiaMensual membresia : membresias.listar()) {
            if (membresia.cubrePeriodo(periodo)) {
                usuariosConMembresia.putIfAbsent(
                        membresia.getUsuario().getId(),
                        membresia
                );
            }
        }

        Map<Long, PagoMensual> pagosPorUsuario = pagos
                .listarPorPeriodo(periodo.toString())
                .stream()
                .collect(Collectors.toMap(
                        pago -> pago.getUsuario().getId(),
                        pago -> pago,
                        (primero, ignorado) -> primero
                ));

        String estadoSinPago = periodo.isBefore(YearMonth.now())
                ? "VENCIDO"
                : "PENDIENTE";

        List<EstadoMensualidadResponse> resultado = new ArrayList<>();
        usuariosConMembresia.forEach((usuarioId, membresia) -> {
            PagoMensual pago = pagosPorUsuario.get(usuarioId);
            Usuario usuario = membresia.getUsuario();
            resultado.add(new EstadoMensualidadResponse(
                    usuarioId,
                    usuario.getNombres(),
                    usuario.getNumeroDocumento(),
                    periodo.toString(),
                    pago == null
                            ? estadoSinPago
                            : pago.getEstado().name(),
                    pago == null ? null : pago.getValor(),
                    pago == null ? null : pago.getMetodo(),
                    pago == null ? null : pago.getFechaPago()
            ));
        });

        return resultado;
    }

    @Transactional(readOnly = true)
    public List<PagoMensualResponse> listarPagos(String periodoSolicitado) {
        List<PagoMensual> resultado;
        if (periodoSolicitado == null || periodoSolicitado.isBlank()) {
            resultado = pagos.listar();
        } else {
            YearMonth periodo = validarPeriodo(periodoSolicitado);
            resultado = pagos.listarPorPeriodo(periodo.toString());
        }
        return resultado.stream().map(this::convertirPago).toList();
    }

    private YearMonth validarPeriodo(String valor) {
        if (valor == null || !valor.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new IllegalArgumentException(
                    "El período debe tener el formato YYYY-MM."
            );
        }

        try {
            YearMonth periodo = YearMonth.parse(valor);
            if (periodo.isAfter(YearMonth.now())) {
                throw new IllegalArgumentException(
                        "No se pueden registrar ni consultar períodos futuros."
                );
            }
            return periodo;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "El período indicado no es válido."
            );
        }
    }

    private BigDecimal validarValor(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0
                || valor.scale() > 2
                || valor.precision() > 12) {
            throw new IllegalArgumentException(
                    "El valor debe ser positivo y tener máximo dos decimales."
            );
        }
        return valor;
    }

    private MembresiaMensualResponse convertirMembresia(
            MembresiaMensual membresia) {
        Usuario usuario = membresia.getUsuario();
        return new MembresiaMensualResponse(
                membresia.getId(),
                usuario.getId(),
                usuario.getNombres(),
                usuario.getNumeroDocumento(),
                membresia.getFechaInicio(),
                membresia.getFechaFin(),
                membresia.getEstado().name()
        );
    }

    private PagoMensualResponse convertirPago(PagoMensual pago) {
        Usuario usuario = pago.getUsuario();
        return new PagoMensualResponse(
                pago.getId(),
                usuario.getId(),
                usuario.getNombres(),
                pago.getPeriodo(),
                pago.getValor(),
                pago.getMetodo(),
                pago.getEstado().name(),
                pago.getFechaPago()
        );
    }
}
