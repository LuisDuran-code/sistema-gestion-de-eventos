package com.eventos.dto;

import java.time.LocalDateTime;

public record InscritoDto(
        Long usuarioId,
        String nombre,
        String email,
        LocalDateTime fechaInscripcion
) {}
