package com.desafio_tecnico.tarifa_agua.dto;

import com.desafio_tecnico.tarifa_agua.entity.CategoriaConsumidor;

import java.util.List;

public record CategoriaResponse(
        CategoriaConsumidor categoria,
        List<FaixaResponse> faixas
) {}

