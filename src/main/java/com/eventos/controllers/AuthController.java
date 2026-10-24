package com.eventos.controllers;

import com.eventos.dto.LoginRequestDto;
import com.eventos.dto.RegistroRequestDto;
import com.eventos.dto.UsuarioResponseDto;
import com.eventos.services.UsuarioService;
import com.eventos.services.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @PostMapping("/login")
    public UsuarioResponseDto login(@Valid @RequestBody LoginRequestDto req,
                                    HttpServletRequest request, HttpServletResponse response) {
        return authService.login(req, request, response);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of("email", auth.getName(),
                "roles", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDto registro(@Valid @RequestBody RegistroRequestDto req) {
        return authService.registrar(req);
    }
}