package com.desafio_tecnico.tarifa_agua.repository;

import com.desafio_tecnico.tarifa_agua.entity.FaixaConsumo;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FaixaConsumoRepository extends JpaRepository<FaixaConsumo, Long> {
}
