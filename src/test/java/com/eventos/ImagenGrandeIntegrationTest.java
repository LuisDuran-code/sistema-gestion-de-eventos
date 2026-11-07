package com.eventos;

import com.eventos.entities.Usuario;
import com.eventos.enums.Rol;
import com.eventos.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Corre sobre un servidor real: MockMvc no aplica el límite de tamaño de Tomcat. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ImagenGrandeIntegrationTest {

    @Autowired TestRestTemplate rest;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    @Test
    void unaImagenDeMasDeCuatroMegasDevuelveUnMensajeClaro() {
        String email = "grande@prueba.test";
        String clave = "clave-de-prueba-1";
        usuarios.findByEmail(email).orElseGet(() -> usuarios.save(Usuario.builder()
                .nombre("Admin").email(email).passwordHash(encoder.encode(clave)).rol(Rol.ADMIN).activo(true).build()));

        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> login = rest.postForEntity("/api/auth/login",
                new HttpEntity<>("{\"email\":\"" + email + "\",\"password\":\"" + clave + "\"}", json), String.class);
        ResponseEntity<String> me = rest.exchange("/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(conCookies(login, null)), String.class);

        HttpHeaders cabeceras = conCookies(login, me);
        cabeceras.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("archivo", new ByteArrayResource(new byte[4 * 1024 * 1024 + 1]) {
            @Override
            public String getFilename() {
                return "grande.jpg";
            }
        });

        ResponseEntity<Map> respuesta = rest.exchange("/api/imagenes", HttpMethod.POST, new HttpEntity<>(cuerpo, cabeceras), Map.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(respuesta.getBody().get("message")).isEqualTo("La imagen supera los 4 MB");
    }

    private HttpHeaders conCookies(ResponseEntity<?> login, ResponseEntity<?> me) {
        List<String> crudas = new java.util.ArrayList<>(login.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
        if (me != null) {
            crudas.addAll(me.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE));
        }
        Map<String, String> cookies = crudas.stream().map(c -> c.split(";")[0].split("=", 2))
                .collect(Collectors.toMap(p -> p[0], p -> p[1], (a, b) -> b));
        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.add(HttpHeaders.COOKIE, cookies.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("; ")));
        if (cookies.containsKey("XSRF-TOKEN")) {
            cabeceras.add("X-XSRF-TOKEN", cookies.get("XSRF-TOKEN"));
        }
        return cabeceras;
    }
}
