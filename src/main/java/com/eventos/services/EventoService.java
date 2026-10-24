package com.eventos.services;

import com.eventos.entities.Evento;
import com.eventos.repositories.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    public List<Evento> listar() {
        return eventoRepository.findAll();
    }
}