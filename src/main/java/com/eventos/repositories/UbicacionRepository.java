package com.eventos.repositories;

import com.eventos.entities.Ubicacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    Page<Ubicacion> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Ubicacion> findByActivoTrueAndNombreContainingIgnoreCase(String nombre,
                                                                  Pageable pageable);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Ubicacion u where u.id = :id")
    Optional<Ubicacion> findByIdForUpdate(@Param("id") Long id);

    @Query("""
       select count(e) from Evento e
        where e.ubicacion.id = :ubicacionId
          and e.estado = com.eventos.enums.EstadoEvento.PROGRAMADO
          and (e.fecha > :hoy or (e.fecha = :hoy and e.horaFin > :ahora))
       """)
    long contarVigentes(@Param("ubicacionId") Long ubicacionId,
                        @Param("hoy") LocalDate hoy,
                        @Param("ahora") LocalTime ahora);

    @Query("""
       select count(e) from Evento e
        where e.ubicacion.id = :ubicacionId
          and e.estado = com.eventos.enums.EstadoEvento.PROGRAMADO
          and e.cupo > :capacidad
          and (e.fecha > :hoy or (e.fecha = :hoy and e.horaFin > :ahora))
       """)
    long contarVigentesConCupoMayorA(@Param("ubicacionId") Long ubicacionId,
                                     @Param("capacidad") Integer capacidad,
                                     @Param("hoy") LocalDate hoy,
                                     @Param("ahora") LocalTime ahora);
}