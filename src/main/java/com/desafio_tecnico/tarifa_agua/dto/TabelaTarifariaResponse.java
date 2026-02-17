package com.desafio_tecnico.tarifa_agua.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TabelaTarifariaResponse(
        Long id,
        String nome,
        LocalDateTime dataVigencia,
        boolean ativa,
        List<CategoriaResponse> categorias
) {}
