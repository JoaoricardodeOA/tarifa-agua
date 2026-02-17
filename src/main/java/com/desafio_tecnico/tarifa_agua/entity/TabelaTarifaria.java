package com.desafio_tecnico.tarifa_agua.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tabela_tarifaria")
public class TabelaTarifaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataVigencia;

    @Column(nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "tabelaTarifaria", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TarifaCategoria> categorias = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.dataVigencia = LocalDateTime.now(ZoneId.of("America/Recife"));
    }
}