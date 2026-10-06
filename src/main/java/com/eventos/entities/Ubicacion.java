package com.eventos.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false)
    private Integer capacidad;

    @Column (nullable = false)
    private boolean activo;

    @Column(length = 500)
    private String imagenPrincipal;

    @ElementCollection
    @CollectionTable(name = "ubicacion_imagenes", joinColumns = @JoinColumn(name = "ubicacion_id"))
    @Column(name = "url", length = 500)
    @Builder.Default
    private List<String> imagenes = new ArrayList<>();



}
