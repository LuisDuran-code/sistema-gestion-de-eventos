package com.eventos.dto;

import java.time.LocalTime;

public record InscritoDto(
        Long usuarioId,
        String nombre,
        String email,
        LocalTime fechaInscripcion
) {}
