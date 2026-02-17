package com.desafio_tecnico.tarifa_agua.controller;

import com.desafio_tecnico.tarifa_agua.dto.CalculoRequest;
import com.desafio_tecnico.tarifa_agua.dto.CalculoResponse;
import com.desafio_tecnico.tarifa_agua.service.CalculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calculos")
@RequiredArgsConstructor
public class CalculoController {

    private final CalculoService calculoService;

    @PostMapping
    public ResponseEntity<CalculoResponse> calcular(
            @Valid @RequestBody CalculoRequest request
    ) {
        CalculoResponse response = calculoService.calcular(request);
        return ResponseEntity.ok(response);
    }
}
