package com.eventos.controllers;

import com.eventos.dto.UsuarioResponseDto;
import com.eventos.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public Page<UsuarioResponseDto> listar(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return usuarioService.listar(pageable);
    }
}