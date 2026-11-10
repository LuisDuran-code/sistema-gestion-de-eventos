package com.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Corre sobre un servidor real: MockMvc no pasa por el error dispatcher de Spring Boot. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MensajesDeErrorIntegrationTest {

    @Autowired TestRestTemplate rest;

    @Test
    void unErrorDeNegocioLlegaConSuMensaje() {
        String email = "mensajes@prueba.test";
        String clave = "clave-de-prueba-1";
        rest.postForEntity("/api/auth/registro", json("{\"nombre\":\"Prueba\",\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}"), String.class);
        ResponseEntity<String> login = rest.postForEntity("/api/auth/login", json("{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}"), String.class);
        assertThat(login.getStatusCode().is2xxSuccessful()).isTrue();
        String cookie = login.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
                .filter(c -> c.startsWith("JSESSIONID=")).findFirst().orElseThrow().split(";")[0];

        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.add(HttpHeaders.COOKIE, cookie);
        cabeceras.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<Map> respuesta = rest.exchange("/api/eventos/999999", HttpMethod.GET, new HttpEntity<>(cabeceras), Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody().get("message")).asString().isNotBlank().isNotEqualTo("No message available");
    }

    private HttpEntity<String> json(String cuerpo) {
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(cuerpo, cabeceras);
    }
}
