package com.eventos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequestDto(
        @NotBlank(message = "no debe estar vacío")
        @Size(max = 150, message = "no debe superar los 150 caracteres")
        String nombre,

        @Size(max = 500, message = "no debe superar los 500 caracteres")
        String descripcion,

        @NotNull(message = "no debe ser nulo")
        LocalDate fecha,

        @NotNull(message = "no debe ser nulo")
        LocalTime horaInicio,

        @NotNull(message = "no debe ser nulo")
        LocalTime horaFin,

        @NotNull(message = "no debe ser nulo")
        @Min(value = 1, message = "debe ser mayor o igual a 1")
        Integer cupo,

        @NotNull(message = "no debe ser nulo")
        Long ubicacionId
) {}
