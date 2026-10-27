package com.eventos.mapper;

import com.eventos.dto.UbicacionResponseDto;
import com.eventos.entities.Ubicacion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UbicacionMapper {

    public UbicacionResponseDto aResponse(Ubicacion u) {
        return new UbicacionResponseDto(
                u.getId(), u.getNombre(), u.getDescripcion(), u.getDireccion(),
                u.getCapacidad(), u.isActivo(), u.getImagenPrincipal(),
                new ArrayList<>(u.getImagenes()));
    }
}