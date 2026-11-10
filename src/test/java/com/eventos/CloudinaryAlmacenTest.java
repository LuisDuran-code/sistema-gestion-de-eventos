package com.eventos;

import com.eventos.services.CloudinaryAlmacen;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudinaryAlmacenTest {

    @Test
    void sinConfigurarFallaConUnError500() {
        assertThatThrownBy(() -> new CloudinaryAlmacen("").subir(new byte[]{1}))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> org.assertj.core.api.Assertions.assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void unaUrlMalFormadaNoRevientaConOtroError() {
        assertThatThrownBy(() -> new CloudinaryAlmacen("no-es-una-url").subir(new byte[]{1}))
                .isInstanceOf(ResponseStatusException.class);
    }
}
