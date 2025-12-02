package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.AuthRequest;
import io.github.brunoeugeniodev.marketplace.dto.AuthResponse;
import io.github.brunoeugeniodev.marketplace.dto.UsuarioDTO;
import io.github.brunoeugeniodev.marketplace.dto.UsuarioRegisterDTO;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.AuthService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final MapperUtil mapperUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) throws AuthException {
        AuthResponse response = authService.authenticate(request.getEmail(), request.getSenha());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody UsuarioRegisterDTO registerDTO) throws AuthException {
        // Converte DTO para entidade
        Usuario usuario = mapperUtil.toUsuarioEntity(registerDTO);

        // Registra usuário
        Usuario usuarioSalvo = authService.register(usuario);

        // Converte para DTO de resposta
        UsuarioDTO usuarioDTO = mapperUtil.toUsuarioDTO(usuarioSalvo);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            return usuarioService.buscarPorEmail(userDetails.getUsername())
                    .map(mapperUtil::toUsuarioDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", auth != null && auth.isAuthenticated());

        return ResponseEntity.ok(response);
    }
}