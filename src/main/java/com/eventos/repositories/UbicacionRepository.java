package com.eventos.repositories;

import com.eventos.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepository extends JpaRepository<Evento, Long> {
}