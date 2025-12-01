package io.github.brunoeugeniodev.marketplace.config;

import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class JwtConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UsuarioService usuarioService) {
        return new JwtAuthenticationFilter(jwtUtil, usuarioService);
    }
}