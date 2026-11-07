package com.eventos;

import com.eventos.entities.Evento;
import com.eventos.entities.Ubicacion;
import com.eventos.enums.Rol;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.InscripcionRepository;
import com.eventos.repositories.UbicacionRepository;
import com.eventos.services.AlmacenImagenes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImagenesIntegrationTest {

    private static final String URL_1 = "https://res.cloudinary.com/demo/image/upload/v1/aforo/uno.jpg";
    private static final String URL_2 = "https://res.cloudinary.com/demo/image/upload/v1/aforo/dos.jpg";
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

    @Autowired MockMvc mvc;
    @Autowired UbicacionRepository ubicaciones;
    @Autowired EventoRepository eventos;
    @Autowired InscripcionRepository inscripciones;
    @MockitoBean AlmacenImagenes almacen;

    Ubicacion sala;

    @BeforeEach
    void preparar() {
        inscripciones.deleteAll();
        eventos.deleteAll();
        ubicaciones.deleteAll();
        sala = ubicaciones.save(Ubicacion.builder()
                .nombre("Sala Norte").direccion("Calle 1").capacidad(100).activo(true).build());
        when(almacen.subir(any())).thenReturn(URL_1);
    }

    @Test
    void adminSubeUnaImagenYRecibeSuUrl() throws Exception {
        subir(JPEG, "ADMIN")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url", is(URL_1)));
    }

    @Test
    void aceptaPngYWebp() throws Exception {
        subir(PNG, "ADMIN").andExpect(status().isCreated());
        subir(WEBP, "ADMIN").andExpect(status().isCreated());
    }

    @Test
    void visitanteNoPuedeSubir() throws Exception {
        subir(JPEG, "VISITANTE").andExpect(status().isForbidden());
        verify(almacen, never()).subir(any());
    }

    @Test
    void sinSesionNoPuedeSubir() throws Exception {
        mvc.perform(multipart("/api/imagenes").file(archivo(JPEG)).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenCsrfNoPuedeSubir() throws Exception {
        mvc.perform(multipart("/api/imagenes").file(archivo(JPEG)).with(user("a@b.test").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaUnArchivoQueNoEsImagenAunqueDigaSerlo() throws Exception {
        subir("<svg onload=alert(1)>".getBytes(), "ADMIN").andExpect(status().isUnsupportedMediaType());
        verify(almacen, never()).subir(any());
    }

    @Test
    void rechazaUnArchivoVacio() throws Exception {
        subir(new byte[0], "ADMIN").andExpect(status().isBadRequest());
    }

    @Test
    void unFalloDelAlmacenNoDevuelveUrl() throws Exception {
        when(almacen.subir(any())).thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar la imagen"));

        subir(JPEG, "ADMIN").andExpect(status().isInternalServerError());
    }

    @Test
    void creaUnEventoConSusImagenesEnOrden() throws Exception {
        crearEvento("\"" + URL_2 + "\",\"" + URL_1 + "\"")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagenes", contains(URL_2, URL_1)));
    }

    @Test
    void unEventoSinImagenesDevuelveListaVacia() throws Exception {
        crearEvento("").andExpect(status().isCreated()).andExpect(jsonPath("$.imagenes", empty()));
    }

    @Test
    void elVisitanteVeLasImagenesEnLaListaYEnElDetalle() throws Exception {
        Evento evento = guardarEvento();

        mvc.perform(get("/api/eventos").with(user("a@b.test").roles("VISITANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].imagenes", contains(URL_1, URL_2)));
        mvc.perform(get("/api/eventos/{id}", evento.getId()).with(user("a@b.test").roles("VISITANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes", contains(URL_1, URL_2)));
    }

    @Test
    void editarReemplazaLasImagenesYRespetaElNuevoOrden() throws Exception {
        Evento evento = guardarEvento();

        mvc.perform(put("/api/eventos/{id}", evento.getId()).with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoEvento("\"" + URL_2 + "\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagenes", contains(URL_2)));
    }

    @Test
    void rechazaUnaUrlQueNoEsDeCloudinary() throws Exception {
        crearEvento("\"https://otro-sitio.test/foto.jpg\"").andExpect(status().isBadRequest());
    }

    @Test
    void rechazaMasDeDiezImagenes() throws Exception {
        String[] urls = new String[11];
        Arrays.fill(urls, "\"" + URL_1 + "\"");

        crearEvento(String.join(",", urls)).andExpect(status().isBadRequest());
    }

    @Test
    void laUbicacionTambienRechazaUrlsAjenas() throws Exception {
        mvc.perform(post("/api/ubicaciones").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Patio\",\"direccion\":\"Calle 2\",\"capacidad\":10,"
                                + "\"imagenPrincipal\":\"https://otro-sitio.test/foto.jpg\",\"imagenes\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laUbicacionAceptaUrlsDeCloudinary() throws Exception {
        mvc.perform(post("/api/ubicaciones").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Patio\",\"direccion\":\"Calle 2\",\"capacidad\":10,"
                                + "\"imagenPrincipal\":\"" + URL_1 + "\",\"imagenes\":[\"" + URL_2 + "\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagenPrincipal", is(URL_1)));
    }

    private ResultActions subir(byte[] contenido, String rol) throws Exception {
        return mvc.perform(multipart("/api/imagenes").file(archivo(contenido))
                .with(user("a@b.test").roles(rol)).with(csrf()));
    }

    private MockMultipartFile archivo(byte[] contenido) {
        return new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", contenido);
    }

    private ResultActions crearEvento(String imagenes) throws Exception {
        return mvc.perform(post("/api/eventos").with(admin()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoEvento(imagenes)));
    }

    private String cuerpoEvento(String imagenes) {
        return "{\"nombre\":\"Taller\",\"fecha\":\"" + LocalDate.now().plusDays(3) + "\","
                + "\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"cupo\":5,"
                + "\"ubicacionId\":" + sala.getId() + ",\"imagenes\":[" + imagenes + "]}";
    }

    private Evento guardarEvento() {
        Evento evento = Evento.builder()
                .nombre("Taller").fecha(LocalDate.now().plusDays(3))
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(12, 0))
                .limiteAsistentes(5).ubicacion(sala).activo(true).build();
        evento.getImagenes().add(URL_1);
        evento.getImagenes().add(URL_2);
        return eventos.save(evento);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return user("admin@b.test").roles(Rol.ADMIN.name());
    }
}
