package com.eventos.mapper;

import com.eventos.dto.InscripcionResponseDto;
import com.eventos.entities.Inscripcion;
import org.springframework.stereotype.Component;

@Component
public class InscripcionMapper {

    private final EventoMapper eventoMapper;

    public InscripcionMapper(EventoMapper eventoMapper) {
        this.eventoMapper = eventoMapper;
    }

    public InscripcionResponseDto toResponse(Inscripcion inscripcion) {
        return new InscripcionResponseDto(
            inscripcion.getId(),
            inscripcion.getEstado(),
            inscripcion.getFechaInscripcion(),
            eventoMapper.aResumen(inscripcion.getEvento())
        );
    }
}