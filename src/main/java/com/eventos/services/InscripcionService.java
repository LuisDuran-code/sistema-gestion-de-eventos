package com.eventos.services;

import com.eventos.dtos.InscripcionRequestDto;
import com.eventos.dtos.InscripcionResponseDto;
import com.eventos.entities.Evento;
import com.eventos.entities.Inscripcion;
import com.eventos.entities.Usuario;
import com.eventos.enums.Estado;
import com.eventos.enums.Rol;
import com.eventos.mappers.InscripcionMapper;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.InscripcionRepository;
import com.eventos.repositories.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionMapper inscripcionMapper;

    public InscripcionService(InscripcionRepository inscripcionRepository,
                              EventoRepository eventoRepository,
                              UsuarioRepository usuarioRepository,
                              InscripcionMapper inscripcionMapper) {
        this.inscripcionRepository = inscripcionRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscripcionMapper = inscripcionMapper;
    }

    @Transactional
    public InscripcionResponseDto inscribir(String email, InscripcionRequestDto request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Los administradores no pueden inscribirse");
        }

        Evento evento = eventoRepository.findByIdForUpdate(request.eventoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El evento no existe"));

        if (!evento.isActivo()) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento no está disponible");
}

        LocalDateTime inicioEvento = LocalDateTime.of(evento.getFecha(), evento.getHoraInicio());
        if (inicioEvento.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento ya comenzó");
        }

             var inscripcionExistente = inscripcionRepository.findByUsuarioIdAndEventoId(usuario.getId(), evento.getId());
        if (inscripcionExistente.isPresent() && inscripcionExistente.get().getEstado() == Estado.CONFIRMADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya estás inscrito en este evento");
        }

        boolean haySolape = inscripcionRepository.existeSolapeParaUsuario(
                usuario.getId(), evento.getFecha(), evento.getHoraInicio(), evento.getHoraFin());
        if (haySolape) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tienes otra inscripción activa en este horario");
        }

        long inscritosActivos = inscripcionRepository.countByEventoIdAndEstado(evento.getId(), Estado.CONFIRMADA);
        if (inscritosActivos >= evento.getLimiteAsistentes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El evento está lleno");
        }

        Inscripcion inscripcion = inscripcionExistente.orElseGet(Inscripcion::new);
        inscripcion.setUsuario(usuario);
        inscripcion.setEvento(evento);
        inscripcion.setEstado(Estado.CONFIRMADA);
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        return inscripcionMapper.toResponse(inscripcionRepository.save(inscripcion));
    }

    @Transactional
    public void darDeBaja(String email, Long inscripcionId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscripción no encontrada"));

        if (!inscripcion.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscripción no encontrada");
        }

        if (inscripcion.getEstado() == Estado.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La inscripción ya está cancelada");
        }

        inscripcion.setEstado(Estado.CANCELADA);
        inscripcionRepository.save(inscripcion);
    }

    @Transactional(readOnly = true)
    public Page<InscripcionResponseDto> listarMias(String email, Estado estado, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Page<Inscripcion> pagina = (estado != null)
                ? inscripcionRepository.findByUsuarioIdAndEstado(usuario.getId(), estado, pageable)
                : inscripcionRepository.findByUsuarioId(usuario.getId(), pageable);

        return pagina.map(inscripcionMapper::toResponse);
    }
}
    
