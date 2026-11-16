package co.autoscolombia.application.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String telefono,
        String correo,
        boolean activo,
        LocalDateTime creadoEn) {
}
