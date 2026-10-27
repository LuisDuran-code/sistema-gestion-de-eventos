package com.eventos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoResumenDto(
        Long id,
        String nombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String ubicacionNombre
) {}
