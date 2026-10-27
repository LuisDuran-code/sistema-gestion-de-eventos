package com.eventos.repositories;

import com.eventos.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    boolean existsByUbicacionIdAndActivoTrueAndFechaGreaterThanEqual(Long id, LocalDate hoy);
}
