package co.autoscolombia.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstadoMensualidadResponse(
        Long usuarioId,
        String nombres,
        String numeroDocumento,
        String periodo,
        String estado,
        BigDecimal valor,
        String metodo,
        LocalDateTime fechaPago
) {
}
