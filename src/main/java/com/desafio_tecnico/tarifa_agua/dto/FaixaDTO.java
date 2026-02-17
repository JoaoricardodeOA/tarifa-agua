package com.desafio_tecnico.tarifa_agua.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FaixaDTO(
        @NotNull @Min(0)
        Integer inicio,
        Integer fim,
        @NotNull @DecimalMin("0")
        BigDecimal valorUnitario
) {}
