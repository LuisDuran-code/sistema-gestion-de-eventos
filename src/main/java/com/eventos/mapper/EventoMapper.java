package com.eventos.mapper;

import com.eventos.dto.EventoResponseDto;
import com.eventos.dto.EventoResumenDto;
import com.eventos.dto.InscritoDto;
import com.eventos.dto.UbicacionResumenDto;
import com.eventos.entities.Evento;
import com.eventos.entities.Inscripcion;
import com.eventos.entities.Ubicacion;
import org.springframework.stereotype.Component;

@Component
public class EventoMapper {

    public EventoResponseDto aResponse(Evento evento, long inscritosActivos) {
        Ubicacion u = evento.getUbicacion();
        UbicacionResumenDto ubicacionResumen = new UbicacionResumenDto(
                u.getId(),
                u.getNombre(),
                u.getDireccion()
        );

        int cupoTotal = evento.getLimiteAsistentes();
        long disponibles = Math.max(0, cupoTotal - inscritosActivos);
        String estadoTexto = evento.isActivo() ? "PROGRAMADO" : "CANCELADO";

        return new EventoResponseDto(
                evento.getId(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getFecha(),
                evento.getHoraInicio(),
                evento.getHoraFin(),
                cupoTotal,
                inscritosActivos,
                disponibles,
                estadoTexto,
                ubicacionResumen
        );
    }

    public EventoResumenDto aResumen(Evento evento) {
        return new EventoResumenDto(
                evento.getId(),
                evento.getNombre(),
                evento.getFecha(),
                evento.getHoraInicio(),
                evento.getHoraFin(),
                evento.getUbicacion().getNombre()
        );
    }

    public InscritoDto aInscritoDto(Inscripcion inscripcion) {
        var usuario = inscripcion.getUsuario();
        return new InscritoDto(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                inscripcion.getFechaInscripcion()
        );
    }
}