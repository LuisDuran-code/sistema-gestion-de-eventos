package com.eventos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UbicacionRequestDto(
        @NotBlank @Size(max = 150) String nombre,
        @Size(max = 500) String descripcion,
        @NotBlank @Size(max = 200) String direccion,
        @NotNull @Min(1) Integer capacidad,
        @Size(max = 500) @Pattern(regexp = "^https://\\S+$", message = "Debe ser una URL https")
        String imagenPrincipal,
        List<@NotBlank @Size(max = 500)
                @Pattern(regexp = "^https://\\S+$", message = "Debe ser una URL https")
                        String> imagenes) {}