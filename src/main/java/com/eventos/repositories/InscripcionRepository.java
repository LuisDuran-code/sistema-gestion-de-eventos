package com.eventos.repositories;

import com.eventos.entities.Inscripcion;
import com.eventos.enums.Estado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    long countByEventoIdAndEstado(Long eventoId, Estado estado);

    @EntityGraph(attributePaths = {"usuario"})
    Page<Inscripcion> findByEventoIdAndEstado(Long eventoId, Estado estado, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Inscripcion i
        SET i.estado = :nuevoEstado
        WHERE i.evento.id = :eventoId AND i.estado = :estadoActual
    """)
    void actualizarEstadoPorEvento(
            @Param("eventoId") Long eventoId,
            @Param("estadoActual") Estado estadoActual,
            @Param("nuevoEstado") Estado nuevoEstado
    );
}