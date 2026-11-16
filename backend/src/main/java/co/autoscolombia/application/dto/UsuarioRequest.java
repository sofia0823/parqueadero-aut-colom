package co.autoscolombia.application.dto;

public record UsuarioRequest(
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String telefono,
        String correo) {
}
