package com.eventos.controllers;

import com.eventos.dtos.InscripcionRequestDto;
import com.eventos.dtos.InscripcionResponseDto;
import com.eventos.enums.Estado;
import com.eventos.services.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @PostMapping
    public ResponseEntity<InscripcionResponseDto> inscribir(
            Authentication auth,
            @Valid @RequestBody InscripcionRequestDto request) {
        InscripcionResponseDto response = inscripcionService.inscribir(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> darDeBaja(Authentication auth, @PathVariable Long id) {
        inscripcionService.darDeBaja(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mias")
    public ResponseEntity<Page<InscripcionResponseDto>> listarMias(
            Authentication auth,
            @RequestParam(required = false) Estado estado,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<InscripcionResponseDto> pagina = inscripcionService.listarMias(auth.getName(), estado, pageable);
        return ResponseEntity.ok(pagina);
    }
}