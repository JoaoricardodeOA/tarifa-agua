package com.desafio_tecnico.tarifa_agua.repository;

import com.desafio_tecnico.tarifa_agua.entity.TarifaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaCategoriaRepository extends JpaRepository<TarifaCategoria, Long> {
}
