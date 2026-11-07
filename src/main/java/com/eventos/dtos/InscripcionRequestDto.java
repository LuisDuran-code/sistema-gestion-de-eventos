package com.eventos.dtos;

import jakarta.validation.constraints.NotNull;

public record InscripcionRequestDto(
    @NotNull(message = "El ID del evento no puede ser nulo")
    Long eventoId
) {}