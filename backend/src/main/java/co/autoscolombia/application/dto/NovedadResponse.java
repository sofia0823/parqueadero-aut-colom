package co.autoscolombia.application.dto;

import java.time.LocalDateTime;

public record NovedadResponse(
        Long id,
        Long vehiculoId,
        String placa,
        Long estanciaId,
        String descripcion,
        LocalDateTime fechaHora,
        String responsable) {
}
