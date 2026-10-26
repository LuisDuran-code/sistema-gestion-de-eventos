package com.eventos.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoResponseDto(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cupo,
        long inscritos,
        long cuposDisponibles,
        String estado,
        UbicacionResumenDto ubicacion
) {}