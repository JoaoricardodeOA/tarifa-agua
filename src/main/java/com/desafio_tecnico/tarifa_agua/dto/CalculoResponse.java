package com.desafio_tecnico.tarifa_agua.dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculoResponse(
        String categoria,
        Integer consumoTotal,
        BigDecimal valorTotal,
        List<DetalhamentoResponse> detalhamento
) {}
