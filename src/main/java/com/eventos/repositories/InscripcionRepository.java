package com.eventos.repositories;

import com.eventos.entities.Inscripcion;
import com.eventos.enums.Estado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByUsuarioIdAndEventoId(Long usuarioId, Long eventoId);

    long countByEventoIdAndEstado(Long eventoId, Estado estado);

    @EntityGraph(attributePaths = {"evento", "evento.ubicacion"})
    Page<Inscripcion> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"evento", "evento.ubicacion"})
    Page<Inscripcion> findByUsuarioIdAndEstado(Long usuarioId, Estado estado, Pageable pageable);

    @Query("""
        SELECT COUNT(i) > 0 
        FROM Inscripcion i 
        WHERE i.usuario.id = :usuarioId 
          AND i.estado = 'ACTIVA' 
          AND i.evento.estado = 'PROGRAMADO' 
          AND i.evento.fecha = :fecha 
          AND i.evento.horaInicio < :horaFin 
          AND i.evento.horaFin > :horaInicio
    """)
    boolean existeSolapeParaUsuario(
        @Param("usuarioId") Long usuarioId,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );
}