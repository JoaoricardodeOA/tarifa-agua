package com.desafio_tecnico.tarifa_agua.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {}
