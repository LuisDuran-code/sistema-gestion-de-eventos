package com.eventos.mappers;

import com.eventos.dtos.InscripcionResponseDto;
import com.eventos.entities.Inscripcion;
import org.springframework.stereotype.Component;

@Component
public class InscripcionMapper {

    public InscripcionResponseDto toResponse(Inscripcion inscripcion) {
        return new InscripcionResponseDto(
            inscripcion.getId(),
            inscripcion.getEstado(),
            inscripcion.getFechaInscripcion(),
            inscripcion.getEvento()
        );
    }
}