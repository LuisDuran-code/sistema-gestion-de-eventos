package com.eventos.repositories;

import com.eventos.entities.Evento;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import java.time.LocalDate;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    @EntityGraph(attributePaths = {"ubicacion"})
    @Query("""
        SELECT e FROM Evento e
        WHERE (:nombre IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%')))
          AND (:desde IS NULL OR e.fecha >= :desde)
          AND (:hasta IS NULL OR e.fecha <= :hasta)
          AND (:ubicacionId IS NULL OR e.ubicacion.id = :ubicacionId)
          AND (:activo IS NULL OR e.activo = :activo)
    """)
    Page<Evento> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta,
            @Param("ubicacionId") Long ubicacionId,
            @Param("activo") Boolean activo,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Evento e WHERE e.id = :id")
    Optional<Evento> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT COUNT(e) > 0 FROM Evento e
        WHERE e.ubicacion.id = :ubicacionId
          AND e.activo = true
          AND e.fecha = :fecha
          AND (:excluirId IS NULL OR e.id <> :excluirId)
          AND e.horaInicio < :fin
          AND e.horaFin > :inicio
    """)
    boolean existeSolapeEnUbicacion(
            @Param("ubicacionId") Long ubicacionId,
            @Param("fecha") LocalDate fecha,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin,
            @Param("excluirId") Long excluirId
    );

    boolean existsByUbicacionIdAndActivoTrueAndFechaGreaterThanEqual(
            Long ubicacionId,
            LocalDate fecha
    );
}
