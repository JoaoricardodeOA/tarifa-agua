package com.desafio_tecnico.tarifa_agua.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "faixa_consumo")
@Getter
@Setter
@NoArgsConstructor
public class FaixaConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tarifa_categoria_id", nullable = false)
    private TarifaCategoria tarifaCategoria;

    @Column(nullable = false)
    private Integer inicio;

    private Integer fim;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valorUnitario;
}