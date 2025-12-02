package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.config.JwtUtil;
import io.github.brunoeugeniodev.marketplace.dto.AuthResponse;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse authenticate(String email, String password) throws AuthException {
        try {
            // Autentica via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Obtém UserDetails do Spring Security
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Gera token
            String token = jwtUtil.generateToken(userDetails);

            // Busca usuário completo para obter nome
            Usuario usuario = usuarioService.buscarPorEmail(email)
                    .orElseThrow(() -> new AuthException("Usuário não encontrado"));

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .email(userDetails.getUsername())
                    .nome(usuario.getNome())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList()))
                    .expiresIn(jwtUtil.getTimeUntilExpiration(token))
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Tentativa de login com credenciais inválidas: {}", email);
            throw new AuthException("Credenciais inválidas");
        } catch (Exception e) {
            log.error("Erro durante autenticação: {}", e.getMessage(), e);
            throw new AuthException("Erro ao autenticar");
        }
    }

    public Usuario register(Usuario usuario) throws AuthException {
        // Valida se email já existe
        if (usuarioService.buscarPorEmail(usuario.getEmail()).isPresent()) {
            throw new AuthException("Email já está em uso");
        }

        // Valida se CPF já existe
        if (usuarioService.existsByCpf(usuario.getCpf())) {
            throw new AuthException("CPF já está em uso");
        }

        // Codifica senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        // Define role padrão
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.setRoles(List.of("ROLE_USER"));
        }

        // Define ativo como true
        usuario.setAtivo(true);

        // Salva usuário
        Usuario usuarioSalvo = usuarioService.salvarUsuario(usuario);

        // Cria carrinho para o usuário
        usuarioService.criarCarrinhoParaUsuario(usuarioSalvo);

        return usuarioSalvo;
    }
}