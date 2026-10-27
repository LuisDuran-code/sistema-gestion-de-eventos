package com.eventos.controllers;

import com.eventos.dto.UbicacionRequestDto;
import com.eventos.dto.UbicacionResponseDto;
import com.eventos.services.UbicacionService;
import com.eventos.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public Page<UbicacionResponseDto> listar(
            @RequestParam(required = false) String nombre,
            @PageableDefault(size = 10, sort = "nombre") Pageable pageable,
            Authentication auth) {
        return ubicacionService.listar(nombre, pageable, AuthUtil.esAdmin(auth));
    }

    @GetMapping("/{id}")
    public UbicacionResponseDto obtener(@PathVariable Long id, Authentication auth) {
        return ubicacionService.obtener(id, AuthUtil.esAdmin(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UbicacionResponseDto crear(@Valid @RequestBody UbicacionRequestDto req) {
        return ubicacionService.crear(req);
    }

    @PutMapping("/{id}")
    public UbicacionResponseDto actualizar(@PathVariable Long id,
                                           @Valid @RequestBody UbicacionRequestDto req) {
        return ubicacionService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        ubicacionService.desactivar(id);
    }

    @PatchMapping("/{id}/reactivar")
    public UbicacionResponseDto reactivar(@PathVariable Long id) {
        return ubicacionService.reactivar(id);
    }
}