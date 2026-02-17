package com.desafio_tecnico.tarifa_agua.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CategoriaDTO(
        @NotBlank
        String categoria,
        @NotEmpty @Valid
        List<FaixaDTO> faixas
) {}
