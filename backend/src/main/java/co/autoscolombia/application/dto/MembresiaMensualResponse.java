package co.autoscolombia.application.dto;

import java.time.LocalDate;

public record MembresiaMensualResponse(
        Long id,
        Long usuarioId,
        String nombres,
        String numeroDocumento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado
) {
}
