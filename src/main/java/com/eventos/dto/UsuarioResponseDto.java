package com.eventos.dto;

import com.eventos.enums.Rol;

public record UsuarioResponseDto(Long id, String nombre, String email, Rol rol) {}