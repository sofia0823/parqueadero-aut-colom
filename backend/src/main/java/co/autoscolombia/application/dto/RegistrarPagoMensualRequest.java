package co.autoscolombia.application.dto;

import java.math.BigDecimal;

public record RegistrarPagoMensualRequest(
        Long usuarioId,
        String periodo,
        BigDecimal valor,
        String metodo
) {
}
