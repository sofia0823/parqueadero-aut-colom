package co.autoscolombia.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LiquidacionEstancia(
    Long estanciaId,
    String placa,
    LocalDateTime entrada,
    LocalDateTime fechaCalculo,
    long minutosEstancia,
    long horasFacturadas,
    BigDecimal tarifaPorHora,
    BigDecimal total
) {}