package com.eventos.controllers;

import com.eventos.dto.EventoRequestDto;
import com.eventos.dto.EventoResponseDto;
import com.eventos.dto.InscritoDto;
import com.eventos.services.EventoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<Page<EventoResponseDto>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long ubicacionId,
            @PageableDefault(size = 10) Pageable pageable,
            Authentication auth) {

        return ResponseEntity.ok(
                eventoService.listar(nombre, desde, hasta, ubicacionId, pageable, esAdmin(auth))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDto> obtener(
            @PathVariable Long id,
            Authentication auth) {

        return ResponseEntity.ok(eventoService.obtener(id, esAdmin(auth)));
    }

    @PostMapping
    public ResponseEntity<EventoResponseDto> crear(@Valid @RequestBody EventoRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EventoRequestDto dto) {

        return ResponseEntity.ok(eventoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        eventoService.cancelar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/inscritos")
    public ResponseEntity<Page<InscritoDto>> listarInscritos(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(eventoService.listarInscritos(id, pageable));
    }

    private boolean esAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }
}
