package com.eventos;

import com.eventos.entities.Evento;
import com.eventos.entities.Inscripcion;
import com.eventos.entities.Ubicacion;
import com.eventos.entities.Usuario;
import com.eventos.enums.Estado;
import com.eventos.enums.Rol;
import com.eventos.repositories.EventoRepository;
import com.eventos.repositories.InscripcionRepository;
import com.eventos.repositories.UbicacionRepository;
import com.eventos.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InscripcionesIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired UbicacionRepository ubicaciones;
    @Autowired EventoRepository eventos;
    @Autowired InscripcionRepository inscripciones;

    Usuario visitante;
    Usuario otroVisitante;
    Usuario administrador;
    Ubicacion sala;
    Evento evento;

    @BeforeEach
    void preparar() {
        inscripciones.deleteAll();
        eventos.deleteAll();
        ubicaciones.deleteAll();
        usuarios.deleteAll();

        visitante = crearUsuario("ana@prueba.test", Rol.VISITANTE);
        otroVisitante = crearUsuario("beto@prueba.test", Rol.VISITANTE);
        administrador = crearUsuario("admin@prueba.test", Rol.ADMIN);
        sala = ubicaciones.save(Ubicacion.builder()
                .nombre("Sala Norte").direccion("Calle 1").capacidad(100).activo(true).build());
        evento = crearEvento("Taller", LocalDate.now().plusDays(3), 2);
    }

    @Test
    void inscribirDevuelveElResumenDelEvento() throws Exception {
        mvc.perform(post("/api/inscripciones").with(como(visitante)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":" + evento.getId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("CONFIRMADA")))
                .andExpect(jsonPath("$.evento.id", is(evento.getId().intValue())))
                .andExpect(jsonPath("$.evento.nombre", is("Taller")))
                .andExpect(jsonPath("$.evento.ubicacionNombre", is("Sala Norte")));
    }

    @Test
    void misInscripcionesDevuelveElResumenDelEvento() throws Exception {
        inscribirDirecto(visitante, evento);

        mvc.perform(get("/api/inscripciones/mias").param("estado", "CONFIRMADA").with(como(visitante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].evento.ubicacionNombre", is("Sala Norte")));
    }

    @Test
    void listarInscritosDevuelveLaFechaConHora() throws Exception {
        inscribirDirecto(visitante, evento);

        mvc.perform(get("/api/eventos/{id}/inscritos", evento.getId()).with(como(administrador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].usuarioId", is(visitante.getId().intValue())))
                .andExpect(jsonPath("$.content[0].fechaInscripcion", is("2026-11-05T09:30:00")));
    }

    @Test
    void adminAgregaAsistente() throws Exception {
        agregar(visitante, administrador)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado", is("CONFIRMADA")))
                .andExpect(jsonPath("$.evento.ubicacionNombre", is("Sala Norte")));

        assertThat(inscripciones.countByEventoIdAndEstado(evento.getId(), Estado.CONFIRMADA)).isEqualTo(1);
    }

    @Test
    void adminReactivaUnaInscripcionCancelada() throws Exception {
        Inscripcion cancelada = inscribirDirecto(visitante, evento);
        cancelada.setEstado(Estado.CANCELADA);
        inscripciones.save(cancelada);

        agregar(visitante, administrador)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(cancelada.getId().intValue())));
    }

    @Test
    void adminNoAgregaAUsuarioInexistente() throws Exception {
        mvc.perform(post("/api/eventos/{id}/inscritos", evento.getId()).with(como(administrador)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":999999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminNoAgregaSinUsuarioId() throws Exception {
        mvc.perform(post("/api/eventos/{id}/inscritos", evento.getId()).with(como(administrador)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminNoInscribeAOtroAdministrador() throws Exception {
        agregar(administrador, administrador).andExpect(status().isForbidden());
    }

    @Test
    void adminNoAgregaEnEventoLleno() throws Exception {
        Evento pequeno = crearEvento("Pequeño", LocalDate.now().plusDays(4), 1);
        inscribirDirecto(otroVisitante, pequeno);

        mvc.perform(post("/api/eventos/{id}/inscritos", pequeno.getId()).with(como(administrador)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + visitante.getId() + "}"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage()).isEqualTo("El evento está lleno"));
    }

    @Test
    void adminNoAgregaEnEventoYaComenzado() throws Exception {
        Evento pasado = crearEvento("Pasado", LocalDate.now().minusDays(1), 5);

        mvc.perform(post("/api/eventos/{id}/inscritos", pasado.getId()).with(como(administrador)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":" + visitante.getId() + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void elMensajeDeDuplicadoHablaDelUsuarioNoDelAdmin() throws Exception {
        inscribirDirecto(visitante, evento);

        agregar(visitante, administrador)
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage())
                        .isEqualTo("El usuario ya está inscrito en este evento"));
    }

    @Test
    void elMensajeDeSolapeHablaDelUsuarioNoDelAdmin() throws Exception {
        Evento mismoHorario = crearEvento("Otro", evento.getFecha(), 5);
        inscribirDirecto(visitante, mismoHorario);

        agregar(visitante, administrador)
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage())
                        .isEqualTo("El usuario tiene otra inscripción activa en este horario"));
    }

    @Test
    void visitanteNoPuedeAgregarAsistentes() throws Exception {
        agregar(visitante, visitante).andExpect(status().isForbidden());
    }

    @Test
    void adminQuitaAsistenteYLiberaElCupo() throws Exception {
        Inscripcion activa = inscribirDirecto(visitante, evento);

        mvc.perform(delete("/api/eventos/{id}/inscritos/{usuarioId}", evento.getId(), visitante.getId())
                        .with(como(administrador)).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(inscripciones.findById(activa.getId()).orElseThrow().getEstado()).isEqualTo(Estado.CANCELADA);
        assertThat(inscripciones.countByEventoIdAndEstado(evento.getId(), Estado.CONFIRMADA)).isZero();
    }

    @Test
    void quitarAQuienNoEstaInscritoDa404() throws Exception {
        mvc.perform(delete("/api/eventos/{id}/inscritos/{usuarioId}", evento.getId(), visitante.getId())
                        .with(como(administrador)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void quitarUnaInscripcionYaCanceladaDa404() throws Exception {
        Inscripcion cancelada = inscribirDirecto(visitante, evento);
        cancelada.setEstado(Estado.CANCELADA);
        inscripciones.save(cancelada);

        mvc.perform(delete("/api/eventos/{id}/inscritos/{usuarioId}", evento.getId(), visitante.getId())
                        .with(como(administrador)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void visitanteNoPuedeQuitarAsistentes() throws Exception {
        inscribirDirecto(otroVisitante, evento);

        mvc.perform(delete("/api/eventos/{id}/inscritos/{usuarioId}", evento.getId(), otroVisitante.getId())
                        .with(como(visitante)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions agregar(Usuario asistente, Usuario quien) throws Exception {
        return mvc.perform(post("/api/eventos/{id}/inscritos", evento.getId()).with(como(quien)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":" + asistente.getId() + "}"));
    }

    private RequestPostProcessor como(Usuario usuario) {
        return user(usuario.getEmail()).roles(usuario.getRol().name());
    }

    private Usuario crearUsuario(String email, Rol rol) {
        return usuarios.save(Usuario.builder()
                .nombre(email).email(email).passwordHash("sin-uso").rol(rol).activo(true).build());
    }

    private Evento crearEvento(String nombre, LocalDate fecha, int limite) {
        return eventos.save(Evento.builder()
                .nombre(nombre).fecha(fecha)
                .horaInicio(LocalTime.of(10, 0)).horaFin(LocalTime.of(12, 0))
                .limiteAsistentes(limite).ubicacion(sala).activo(true).build());
    }

    private Inscripcion inscribirDirecto(Usuario usuario, Evento destino) {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuario);
        inscripcion.setEvento(destino);
        inscripcion.setEstado(Estado.CONFIRMADA);
        inscripcion.setFechaInscripcion(LocalDateTime.of(2026, 11, 5, 9, 30));
        return inscripciones.save(inscripcion);
    }
}
