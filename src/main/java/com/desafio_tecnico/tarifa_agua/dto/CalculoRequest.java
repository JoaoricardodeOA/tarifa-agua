package com.desafio_tecnico.tarifa_agua.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalculoRequest(
        @NotBlank
        String categoria,
        @NotNull @Min(0)
        Integer consumo
) {}
