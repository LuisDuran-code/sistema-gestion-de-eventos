package com.eventos.repositories;

import com.eventos.entities.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Evento, Long> {
}