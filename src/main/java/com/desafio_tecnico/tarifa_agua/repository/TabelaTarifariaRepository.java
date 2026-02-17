package com.desafio_tecnico.tarifa_agua.repository;

import com.desafio_tecnico.tarifa_agua.entity.TabelaTarifaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TabelaTarifariaRepository extends JpaRepository<TabelaTarifaria, Long> {
    @Query("""
    SELECT t FROM TabelaTarifaria t
    LEFT JOIN FETCH t.categorias c
    LEFT JOIN FETCH c.faixas
    WHERE t.ativa = true
""")
    Optional<TabelaTarifaria> findAtivaComRelacionamentos();

    Optional<TabelaTarifaria> findFirstByAtivaTrue();

    @EntityGraph(attributePaths = {"categorias", "categorias.faixas"})
    Page<TabelaTarifaria> findAll(Pageable pageable);

}

