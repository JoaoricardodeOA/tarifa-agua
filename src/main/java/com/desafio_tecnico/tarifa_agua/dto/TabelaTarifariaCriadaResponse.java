package com.desafio_tecnico.tarifa_agua.dto;

import java.time.LocalDateTime;

public record TabelaTarifariaCriadaResponse(
        Long id,
        LocalDateTime dataVigencia,
        String mensagem
) {}