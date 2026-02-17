package com.desafio_tecnico.tarifa_agua.dto;

import java.math.BigDecimal;

public record FaixaResponse(
        Integer inicio,
        Integer fim,
        BigDecimal valorUnitario
) {}