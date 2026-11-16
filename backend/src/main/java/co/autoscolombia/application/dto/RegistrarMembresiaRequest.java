package co.autoscolombia.application.dto;

import java.time.LocalDate;

public record RegistrarMembresiaRequest(
        Long usuarioId,
        LocalDate fechaInicio
) {
}
