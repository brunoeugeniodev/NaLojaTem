package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // APENAS estas dependências - NÃO inclua SecurityConfig
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario salvarUsuario(Usuario usuario) {
        // Validações antes de salvar
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já está em uso: " + usuario.getEmail());
        }

        if (usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new RuntimeException("CPF já está em uso: " + usuario.getCpf());
        }

        // Garante que tenha pelo menos uma role
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.setRoles(List.of("ROLE_USER"));
        }

        String senhaHasheada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaHasheada);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> editarUsuario(Long id, Usuario usuarioNovo) {
        return usuarioRepository.findById(id)
                .map(usuarioExistente -> {
                    // Verifica se o email foi alterado e se já existe
                    if (!usuarioExistente.getEmail().equals(usuarioNovo.getEmail()) &&
                            usuarioRepository.existsByEmail(usuarioNovo.getEmail())) {
                        throw new RuntimeException("Email já está em uso: " + usuarioNovo.getEmail());
                    }

                    // Verifica se o CPF foi alterado e se já existe
                    if (!usuarioExistente.getCpf().equals(usuarioNovo.getCpf()) &&
                            usuarioRepository.existsByCpf(usuarioNovo.getCpf())) {
                        throw new RuntimeException("CPF já está em uso: " + usuarioNovo.getCpf());
                    }

                    usuarioExistente.setNome(usuarioNovo.getNome());
                    usuarioExistente.setCpf(usuarioNovo.getCpf());
                    usuarioExistente.setEmail(usuarioNovo.getEmail());

                    if (usuarioNovo.getSenha() != null && !usuarioNovo.getSenha().isEmpty()) {
                        String senhaHasheada = passwordEncoder.encode(usuarioNovo.getSenha());
                        usuarioExistente.setSenha(senhaHasheada);
                    }

                    return usuarioRepository.save(usuarioExistente);
                });
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> listarUsuarioId(Long id) {
        return usuarioRepository.findById(id);
    }

    public boolean removerUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(usuario.getRoles().stream()
                        .map(role -> role.replace("ROLE_", ""))
                        .toArray(String[]::new))
                .build();
    }
}