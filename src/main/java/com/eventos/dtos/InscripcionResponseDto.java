package com.eventos.dtos;

import com.eventos.enums.Estado;
import java.time.LocalDateTime;

public record InscripcionResponseDto(
    Long id,
    Estado estado,
    LocalDateTime fechaInscripcion,
    Object evento
) {}