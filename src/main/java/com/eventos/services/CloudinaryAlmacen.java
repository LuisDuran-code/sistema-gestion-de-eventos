package com.eventos.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryAlmacen implements AlmacenImagenes {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryAlmacen.class);

    private final String url;

    public CloudinaryAlmacen(@Value("${app.cloudinary.url:}") String url) {
        this.url = url;
    }

    @Override
    public String subir(byte[] contenido) {
        if (url.isBlank()) {
            log.error("Falta la variable CLOUDINARY_URL");
            throw fallo();
        }
        try {
            Map<?, ?> respuesta = new Cloudinary(url).uploader().upload(
                    contenido, ObjectUtils.asMap("folder", "aforo", "resource_type", "image"));
            return (String) respuesta.get("secure_url");
        } catch (IOException | RuntimeException e) {
            log.error("Cloudinary rechazó la imagen: {}", e.getClass().getSimpleName());
            throw fallo();
        }
    }

    private ResponseStatusException fallo() {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen");
    }
}
