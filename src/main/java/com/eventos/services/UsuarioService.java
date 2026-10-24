package com.eventos.services;

import com.eventos.dto.UsuarioResponseDto;
import com.eventos.entities.Usuario;
import com.eventos.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public Page<UsuarioResponseDto> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::aResponse);
    }

    private UsuarioResponseDto aResponse(Usuario u) {
        return new UsuarioResponseDto(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }
}