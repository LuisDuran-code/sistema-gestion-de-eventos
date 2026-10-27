package com.eventos.config;

import com.eventos.entities.Usuario;
import com.eventos.enums.Rol;
import com.eventos.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:}") String email,
                       @Value("${app.admin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return; // sin variables definidas, no se crea nada
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return; // ya existe: no duplicar ni pisar la contraseña
        }
        Usuario admin = Usuario.builder()
                .nombre("Administrador")
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
        usuarioRepository.save(admin);
        log.info("Usuario administrador inicial creado: {}", email);
    }
}