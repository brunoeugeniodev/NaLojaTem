package io.github.brunoeugeniodev.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // CONSTRUTOR ADICIONADO
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Mudado para STATELESS
                )
                .authorizeHttpRequests(auth -> auth
                        // Páginas públicas
                        .requestMatchers("/", "/login", "/registro",
                                "/lojas", "/loja/**", "/ofertas", "/lancamentos", "/destaques",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/error",
                                "/estilos/**", "/scripts/**", "/imagens/**").permitAll()

                        // APIs públicas
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/busca/**").permitAll()
                        .requestMatchers("/api/produtos/**").permitAll()
                        .requestMatchers("/api/lojas/**").permitAll()

                        // APIs autenticadas
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/minha-loja/**").authenticated()
                        .requestMatchers("/api/carrinho/**").authenticated()
                        .requestMatchers("/api/**").authenticated()

                        // Páginas autenticadas
                        .requestMatchers("/minha-loja", "/carrinho", "/cadastro-loja").authenticated()

                        .anyRequest().permitAll()
                )
                // Desabilitar form login padrão do Spring Security
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}