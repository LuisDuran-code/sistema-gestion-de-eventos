package com.eventos.dto;

import jakarta.validation.constraints.NotNull;

public record InscritoRequestDto(@NotNull Long usuarioId) {}
