package com.desafio_tecnico.tarifa_agua.dto;

import java.math.BigDecimal;

public record DetalhamentoResponse(
        FaixaInfo faixa,
        Integer m3Cobrados,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {}
