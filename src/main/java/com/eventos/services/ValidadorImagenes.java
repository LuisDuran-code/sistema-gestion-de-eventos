package com.eventos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Component
public class ValidadorImagenes {

    private final String prefijoPermitido;

    public ValidadorImagenes(@Value("${app.imagenes.prefijo-permitido:}") String prefijoPermitido) {
        this.prefijoPermitido = prefijoPermitido;
    }

    public void validar(Collection<String> urls) {
        if (prefijoPermitido.isBlank()) {
            return;
        }
        if (urls.stream().anyMatch(url -> !url.startsWith(prefijoPermitido))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las imágenes deben estar en " + prefijoPermitido);
        }
    }
}
