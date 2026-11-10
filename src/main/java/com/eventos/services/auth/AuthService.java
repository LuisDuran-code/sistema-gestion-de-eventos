package com.eventos.services.auth;

import com.eventos.dto.LoginRequestDto;
import com.eventos.dto.RegistroRequestDto;
import com.eventos.dto.UsuarioResponseDto;
import com.eventos.entities.Usuario;
import com.eventos.enums.Rol;
import com.eventos.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDto login(LoginRequestDto req, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.email(), req.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        if (request.getSession(false) != null) {
            request.changeSessionId(); // evita la fijación de sesión
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Usuario u = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        return new UsuarioResponseDto(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }

    public UsuarioResponseDto registrar(RegistroRequestDto req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        Usuario u = usuarioRepository.save(Usuario.builder()
                .nombre(req.nombre())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .rol(Rol.VISITANTE)
                .activo(true)
                .build());
        return new UsuarioResponseDto(u.getId(), u.getNombre(), u.getEmail(), u.getRol());
    }
}