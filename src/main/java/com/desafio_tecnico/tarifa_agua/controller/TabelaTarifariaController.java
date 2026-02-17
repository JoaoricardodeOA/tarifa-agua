package com.desafio_tecnico.tarifa_agua.controller;

import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaCriadaResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaExclusaoResponse;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaRequest;
import com.desafio_tecnico.tarifa_agua.dto.TabelaTarifariaResponse;
import com.desafio_tecnico.tarifa_agua.service.TabelaTarifariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tabelas-tarifarias")
@RequiredArgsConstructor
public class TabelaTarifariaController {

    private final TabelaTarifariaService service;

    @PostMapping
    public ResponseEntity<TabelaTarifariaCriadaResponse> criarTabela(
            @Valid @RequestBody TabelaTarifariaRequest request
    ) {
        TabelaTarifariaCriadaResponse response = service.criarTabela(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TabelaTarifariaExclusaoResponse> excluir(@PathVariable Long id) {
        TabelaTarifariaExclusaoResponse response = service.excluir(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<Page<TabelaTarifariaResponse>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

}