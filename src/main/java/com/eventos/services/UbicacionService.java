package com.eventos.services;

import com.eventos.dto.UbicacionRequestDto;
import com.eventos.dto.UbicacionResponseDto;
import com.eventos.entities.Ubicacion;
import com.eventos.mapper.UbicacionMapper;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final EventoRepository eventoRepository;
    private final UbicacionMapper mapper;
    private static final ZoneId ZONA = ZoneId.of("America/El_Salvador");

    @Value("${app.imagenes.prefijo-permitido:}")
    private String prefijoPermitido;

    @Transactional(readOnly = true)
    public Page<UbicacionResponseDto> listar(String nombre, Pageable pageable,
                                             boolean esAdmin) {
        String filtro = nombre == null ? "" : nombre.trim();
        Page<Ubicacion> pagina = esAdmin
                ? ubicacionRepository.findByNombreContainingIgnoreCase(filtro, pageable)
                : ubicacionRepository
                .findByActivoTrueAndNombreContainingIgnoreCase(filtro, pageable);
        return pagina.map(mapper::aResponse);
    }

    @Transactional(readOnly = true)
    public UbicacionResponseDto obtener(Long id, boolean esAdmin) {
        Ubicacion u = buscar(id);
        if (!esAdmin && !u.isActivo()) {
            throw noEncontrada();
        }
        return mapper.aResponse(u);
    }

    @Transactional
    public UbicacionResponseDto crear(UbicacionRequestDto req) {
        validarUrls(req);
        String nombre = req.nombre().trim();
        if (ubicacionRepository.existsByNombreIgnoreCase(nombre)) {
            throw conflicto("Ya existe una ubicación con ese nombre");
        }
        Ubicacion u = Ubicacion.builder()
                .nombre(nombre)
                .descripcion(req.descripcion())
                .direccion(req.direccion())
                .capacidad(req.capacidad())
                .activo(true)
                .imagenPrincipal(req.imagenPrincipal())
                .imagenes(copiar(req.imagenes()))
                .build();
        return mapper.aResponse(ubicacionRepository.save(u));
    }

    @Transactional
    public UbicacionResponseDto actualizar(Long id, UbicacionRequestDto req) {
        validarUrls(req);
        Ubicacion u = ubicacionRepository.findByIdForUpdate(id)
                .orElseThrow(this::noEncontrada);
        String nombre = req.nombre().trim();
        if (ubicacionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw conflicto("Ya existe otra ubicación con ese nombre");
        }
        u.setNombre(nombre);
        u.setDescripcion(req.descripcion());
        u.setDireccion(req.direccion());
        u.setCapacidad(req.capacidad());
        u.setImagenPrincipal(req.imagenPrincipal());
        u.getImagenes().clear();
        u.getImagenes().addAll(copiar(req.imagenes()));
        return mapper.aResponse(u);
    }

    @Transactional
    public void desactivar(Long id) {
        Ubicacion u = ubicacionRepository.findByIdForUpdate(id)
                .orElseThrow(this::noEncontrada);
        if (!u.isActivo()) {
            return;
        }
        LocalDate hoy = LocalDate.now(ZONA);
        if (eventoRepository.existsByUbicacionIdAndActivoTrueAndFechaGreaterThanEqual(id, hoy)) {
            throw conflicto("No se puede desactivar: la ubicación tiene eventos activos. "
                    + "Cancélalos o muévelos primero");
        }
        u.setActivo(false);
    }

    @Transactional
    public UbicacionResponseDto reactivar(Long id) {
        Ubicacion u = buscar(id);
        u.setActivo(true);
        return mapper.aResponse(u);
    }

    private Ubicacion buscar(Long id) {
        return ubicacionRepository.findById(id).orElseThrow(this::noEncontrada);
    }

    private List<String> copiar(List<String> urls) {
        return urls == null ? new ArrayList<>() : new ArrayList<>(urls);
    }

    private void validarUrls(UbicacionRequestDto req) {
        if (prefijoPermitido.isBlank()) {
            return;
        }
        List<String> todas = copiar(req.imagenes());
        if (req.imagenPrincipal() != null) {
            todas.add(req.imagenPrincipal());
        }
        if (todas.stream().anyMatch(url -> !url.startsWith(prefijoPermitido))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las imágenes deben estar en " + prefijoPermitido);
        }
    }

    private ResponseStatusException noEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Ubicación no encontrada");
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}