package com.desafio_tecnico.tarifa_agua.dto;

public record TabelaTarifariaExclusaoResponse(
        Long id,
        boolean ativa,
        String mensagem
) {}
