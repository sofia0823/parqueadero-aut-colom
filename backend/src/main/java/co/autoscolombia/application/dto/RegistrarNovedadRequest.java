package co.autoscolombia.application.dto;

public record RegistrarNovedadRequest(
        Long vehiculoId,
        Long estanciaId,
        String descripcion) {
}
