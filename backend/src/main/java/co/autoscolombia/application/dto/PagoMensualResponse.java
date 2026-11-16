package co.autoscolombia.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoMensualResponse(
        Long id,
        Long usuarioId,
        String nombres,
        String periodo,
        BigDecimal valor,
        String metodo,
        String estado,
        LocalDateTime fechaPago
) {
}
