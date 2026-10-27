package com.eventos.dto;

import java.util.List;

public record UbicacionResponseDto(
        Long id,
        String nombre,
        String descripcion,
        String direccion,
        Integer capacidad,
        boolean activo,
        String imagenPrincipal,
        List<String> imagenes) {}