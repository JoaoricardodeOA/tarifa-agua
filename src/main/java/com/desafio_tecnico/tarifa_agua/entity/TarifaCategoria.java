package com.desafio_tecnico.tarifa_agua.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tarifa_categoria", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tabela_tarifaria_id", "categoria"})
})
@Getter
@Setter
@NoArgsConstructor
public class TarifaCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tabela_tarifaria_id", nullable = false)
    private TabelaTarifaria tabelaTarifaria;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CategoriaConsumidor categoria;


    @OneToMany(mappedBy = "tarifaCategoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FaixaConsumo> faixas = new HashSet<>();

}
