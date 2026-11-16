package co.autoscolombia.application.dto;

public record ResultadoIdempotente<T>(
        T resultado,
        boolean repetida
) {
}
