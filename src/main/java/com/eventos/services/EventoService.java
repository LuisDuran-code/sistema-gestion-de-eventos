package com.eventos.services;

import com.eventos.dto.EventoRequestDto;
import com.eventos.dto.EventoResponseDto;
import com.eventos.dto.InscritoDto;
import com.eventos.entities.Evento;
import com.eventos.entities.Ubicacion;
import com.eventos.enums.Estado;
import com.eventos.mapper.EventoMapper;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.InscripcionRepository;
import com.eventos.repositories.UbicacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EventoMapper eventoMapper;

    public EventoService(
            EventoRepository eventoRepository,
            UbicacionRepository ubicacionRepository,
            InscripcionRepository inscripcionRepository,
            EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.eventoMapper = eventoMapper;
    }

    @Transactional(readOnly = true)
    public Page<EventoResponseDto> listar(
            String nombre,
            LocalDate desde,
            LocalDate hasta,
            Long ubicacionId,
            Pageable pageable,
            boolean esAdmin) {

        Boolean filtroActivo = esAdmin ? null : Boolean.TRUE;

        return eventoRepository
                .buscarConFiltros(nombre, desde, hasta, ubicacionId, filtroActivo, pageable)
                .map(evento -> {
                    long inscritos = inscripcionRepository.countByEventoIdAndEstado(
                            evento.getId(), Estado.CONFIRMADA
                    );
                    return eventoMapper.aResponse(evento, inscritos);
                });
    }

    @Transactional(readOnly = true)
    public EventoResponseDto obtener(Long id, boolean esAdmin) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!esAdmin && !evento.isActivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        long inscritos = inscripcionRepository.countByEventoIdAndEstado(
                evento.getId(), Estado.CONFIRMADA
        );
        return eventoMapper.aResponse(evento, inscritos);
    }

    @Transactional
    public EventoResponseDto crear(EventoRequestDto dto) {
        validarFechaYHoras(dto.fecha(), dto.horaInicio(), dto.horaFin());

        Ubicacion ubicacion = obtenerYValidarUbicacion(dto.ubicacionId(), dto.cupo());

        boolean haySolape = eventoRepository.existeSolapeEnUbicacion(
                ubicacion.getId(), dto.fecha(), dto.horaInicio(), dto.horaFin(), null
        );
        if (haySolape) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El evento choca en horario con otro evento en la misma ubicación"
            );
        }

        Evento evento = Evento.builder()
                .nombre(dto.nombre().trim())
                .descripcion(dto.descripcion())
                .fecha(dto.fecha())
                .horaInicio(dto.horaInicio())
                .horaFin(dto.horaFin())
                .limiteAsistentes(dto.cupo())
                .ubicacion(ubicacion)
                .activo(true)
                .build();

        Evento guardado = eventoRepository.save(evento);
        return eventoMapper.aResponse(guardado, 0L);
    }

    @Transactional
    public EventoResponseDto actualizar(Long id, EventoRequestDto dto) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!evento.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No se puede editar un evento cancelado"
            );
        }

        validarFechaYHoras(dto.fecha(), dto.horaInicio(), dto.horaFin());

        long inscritosActivos = inscripcionRepository.countByEventoIdAndEstado(
                evento.getId(), Estado.CONFIRMADA
        );

        boolean cambioHorarioOUbicacion = !evento.getFecha().equals(dto.fecha())
                || !evento.getHoraInicio().equals(dto.horaInicio())
                || !evento.getHoraFin().equals(dto.horaFin())
                || !evento.getUbicacion().getId().equals(dto.ubicacionId());

        if (inscritosActivos > 0 && cambioHorarioOUbicacion) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar la fecha, hora o ubicación de un evento que ya tiene inscritos activos"
            );
        }

        if (dto.cupo() < inscritosActivos) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nuevo cupo no puede ser menor que los inscritos actuales (" + inscritosActivos + ")"
            );
        }

        Ubicacion ubicacion = obtenerYValidarUbicacion(dto.ubicacionId(), dto.cupo());

        boolean haySolape = eventoRepository.existeSolapeEnUbicacion(
                ubicacion.getId(), dto.fecha(), dto.horaInicio(), dto.horaFin(), evento.getId()
        );
        if (haySolape) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El evento choca en horario con otro evento en la misma ubicación"
            );
        }

        evento.setNombre(dto.nombre().trim());
        evento.setDescripcion(dto.descripcion());
        evento.setFecha(dto.fecha());
        evento.setHoraInicio(dto.horaInicio()) ;
        evento.setHoraFin(dto.horaFin());
        evento.setLimiteAsistentes(dto.cupo());
        evento.setUbicacion(ubicacion);

        Evento actualizado = eventoRepository.save(evento);
        return eventoMapper.aResponse(actualizado, inscritosActivos);
    }

    @Transactional
    public void cancelar(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!evento.isActivo()) {
            return;
        }

        evento.setActivo(false);
        eventoRepository.save(evento);

        inscripcionRepository.actualizarEstadoPorEvento(
                evento.getId(), Estado.CONFIRMADA, Estado.CANCELADA
        );
    }

    @Transactional(readOnly = true)
    public Page<InscritoDto> listarInscritos(Long id, Pageable pageable) {
        if (!eventoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        return inscripcionRepository
                .findByEventoIdAndEstado(id, Estado.CONFIRMADA, pageable)
                .map(eventoMapper::aInscritoDto);
    }

    private void validarFechaYHoras(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (!horaFin.isAfter(horaInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la hora de inicio"
            );
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La fecha del evento no puede estar en el pasado"
            );
        }
        if (fecha.isEqual(LocalDate.now()) && !horaInicio.isAfter(LocalTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La hora de inicio del evento ya pasó"
            );
        }
    }

    private Ubicacion obtenerYValidarUbicacion(Long ubicacionId, Integer cupo) {
        Ubicacion ubicacion = ubicacionRepository.findById(ubicacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La ubicación no existe"));

        if (!ubicacion.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La ubicación está inactiva"
            );
        }

        if (cupo > ubicacion.getCapacidad()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El cupo supera la capacidad máxima de la ubicación (" + ubicacion.getCapacidad() + ")"
            );
        }

        return ubicacion;
    }
}