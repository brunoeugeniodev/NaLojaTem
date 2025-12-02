package io.github.brunoeugeniodev.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    // Remova a injeção do JwtAuthenticationFilter do construtor
    // Em vez disso, crie um método para obter o filtro

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Lazy JwtAuthenticationFilter jwtAuthenticationFilter // Injete aqui diretamente
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/estilos/**", "/scripts/**", "/imagens/**", "/error").permitAll()

                        // Páginas públicas
                        .requestMatchers("/", "/login", "/registro", "/registro/**",
                                "/lojas/**", "/ofertas", "/lancamentos", "/destaques").permitAll()

                        // APIs públicas
                        .requestMatchers("/api/auth/**", "/api/busca/**",
                                "/api/produtos/public/**", "/api/lojas/public/**",
                                "/api/public/**").permitAll()

                        // Acesso à documentação (se houver)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // APIs protegidas por roles
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/vendedor/**").hasAnyRole("VENDEDOR", "ADMIN")
                        .requestMatchers("/api/minha-loja/**", "/api/carrinho/**",
                                "/api/perfil/**", "/api/pedidos/**").authenticated()

                        // Todas outras APIs exigem autenticação
                        .requestMatchers("/api/**").authenticated()

                        // Permite qualquer requisição web (Thymeleaf)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}