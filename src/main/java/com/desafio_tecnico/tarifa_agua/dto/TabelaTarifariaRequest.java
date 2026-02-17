package com.desafio_tecnico.tarifa_agua.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TabelaTarifariaRequest(
        @NotBlank @Size(max = 255)
        String nome,
        @NotEmpty @Valid
        List<CategoriaDTO> categorias
) {}
