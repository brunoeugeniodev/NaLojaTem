package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.exception.ResourceNotFoundException;
import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.CarrinhoRepository;
import io.github.brunoeugeniodev.marketplace.repository.UsuarioRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando usuário por email: {}", email);

        return usuarioRepository.findByEmail(email)
                .filter(Usuario::getAtivo)
                .map(usuario -> {
                    log.debug("Usuário encontrado: {}", usuario.getEmail());
                    return new User(
                            usuario.getEmail(),
                            usuario.getSenha(),
                            getAuthorities(usuario.getRoles())
                    );
                })
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado ou inativo: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado ou inativo: " + email);
                });
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Usuario loadUsuarioByUsername(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(Usuario::getAtivo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado ou inativo: " + email));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorEmailAtivo(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(Usuario::getAtivo);
    }

    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return usuarioRepository.existsByEmailAndIdNot(email, id);
    }

    public boolean existsByCpf(String cpf) {
        return usuarioRepository.existsByCpf(cpf);
    }

    public boolean existsByCpfAndIdNot(String cpf, Long id) {
        return usuarioRepository.existsByCpfAndIdNot(cpf, id);
    }

    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        log.debug("Salvando usuário: {}", usuario.getEmail());
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        log.debug("Criando novo usuário: {}", usuario.getEmail());

        // Validações
        validarUsuario(usuario);

        // Verifica unicidade
        if (existsByEmail(usuario.getEmail())) {
            throw new ValidationException("Email já está em uso");
        }

        if (existsByCpf(usuario.getCpf())) {
            throw new ValidationException("CPF já está em uso");
        }

        // Codifica senha
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        log.debug("Senha codificada para usuário: {}", usuario.getEmail());

        // Define valores padrão
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.setRoles(List.of("USER")); // Sem ROLE_ prefix, será adicionado no getAuthorities
            log.debug("Roles padrão definidas para usuário: {}", usuario.getEmail());
        }

        usuario.setAtivo(true);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário criado com sucesso: ID={}, Email={}", usuarioSalvo.getId(), usuarioSalvo.getEmail());

        // Cria carrinho automaticamente
        criarCarrinhoParaUsuario(usuarioSalvo);

        return usuarioSalvo;
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new ValidationException("Nome é obrigatório");
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email é obrigatório");
        }

        if (usuario.getCpf() == null || usuario.getCpf().trim().isEmpty()) {
            throw new ValidationException("CPF é obrigatório");
        }

        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new ValidationException("Senha é obrigatória");
        }

        if (usuario.getSenha().length() < 6) {
            throw new ValidationException("Senha deve ter pelo menos 6 caracteres");
        }

        // Valida formato de email simples
        if (!usuario.getEmail().contains("@")) {
            throw new ValidationException("Email inválido");
        }
    }

    @Transactional
    public Usuario atualizarUsuario(Long id, Usuario usuarioAtualizado) {
        log.debug("Atualizando usuário ID: {}", id);

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    // Valida email único se for alterado
                    if (!usuario.getEmail().equals(usuarioAtualizado.getEmail())
                            && existsByEmailAndIdNot(usuarioAtualizado.getEmail(), id)) {
                        throw new ValidationException("Email já está em uso");
                    }
                    usuario.setEmail(usuarioAtualizado.getEmail());

                    // Valida CPF único se for alterado
                    if (!usuario.getCpf().equals(usuarioAtualizado.getCpf())
                            && existsByCpfAndIdNot(usuarioAtualizado.getCpf(), id)) {
                        throw new ValidationException("CPF já está em uso");
                    }
                    usuario.setCpf(usuarioAtualizado.getCpf());

                    // Atualiza outros campos
                    usuario.setNome(usuarioAtualizado.getNome());

                    // Atualiza senha se fornecida
                    if (usuarioAtualizado.getSenha() != null && !usuarioAtualizado.getSenha().isEmpty()) {
                        usuario.setSenha(passwordEncoder.encode(usuarioAtualizado.getSenha()));
                        log.debug("Senha atualizada para usuário ID: {}", id);
                    }

                    // Atualiza roles se fornecido (apenas admin pode fazer isso)
                    if (usuarioAtualizado.getRoles() != null && !usuarioAtualizado.getRoles().isEmpty()) {
                        usuario.setRoles(usuarioAtualizado.getRoles());
                        log.debug("Roles atualizadas para usuário ID: {}", id);
                    }

                    Usuario usuarioAtualizadoSalvo = usuarioRepository.save(usuario);
                    log.info("Usuário atualizado: ID={}, Email={}", usuarioAtualizadoSalvo.getId(), usuarioAtualizadoSalvo.getEmail());
                    return usuarioAtualizadoSalvo;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public void desativarUsuario(Long id) {
        log.debug("Desativando usuário ID: {}", id);

        usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setAtivo(false);
                    Usuario usuarioDesativado = usuarioRepository.save(usuario);
                    log.info("Usuário desativado: ID={}, Email={}", usuarioDesativado.getId(), usuarioDesativado.getEmail());
                    return usuarioDesativado;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public void ativarUsuario(Long id) {
        log.debug("Ativando usuário ID: {}", id);

        usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setAtivo(true);
                    Usuario usuarioAtivado = usuarioRepository.save(usuario);
                    log.info("Usuário ativado: ID={}, Email={}", usuarioAtivado.getId(), usuarioAtivado.getEmail());
                    return usuarioAtivado;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosAtivos() {
        log.debug("Listando usuários ativos");
        return usuarioRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodosUsuarios() {
        log.debug("Listando todos os usuários");
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        log.debug("Buscando usuário por ID: {}", id);
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorIdAtivo(Long id) {
        log.debug("Buscando usuário ativo por ID: {}", id);
        return usuarioRepository.findById(id)
                .filter(Usuario::getAtivo);
    }

    @Transactional
    public void criarCarrinhoParaUsuario(Usuario usuario) {
        if (!carrinhoRepository.existsByUsuarioId(usuario.getId())) {
            Carrinho carrinho = Carrinho.builder()
                    .usuario(usuario)
                    .build();
            carrinhoRepository.save(carrinho);
            log.info("Carrinho criado para usuário: ID={}, Email={}", usuario.getId(), usuario.getEmail());
        } else {
            log.debug("Carrinho já existe para usuário: ID={}", usuario.getId());
        }
    }

    @Transactional
    public void deletarUsuario(Long id) {
        log.debug("Deletando usuário ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        usuarioRepository.delete(usuario);
        log.info("Usuário deletado: ID={}, Email={}", usuario.getId(), usuario.getEmail());
    }

    @Transactional
    public Usuario alterarSenha(Long id, String novaSenha) {
        log.debug("Alterando senha para usuário ID: {}", id);

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setSenha(passwordEncoder.encode(novaSenha));
                    Usuario usuarioAtualizado = usuarioRepository.save(usuario);
                    log.info("Senha alterada para usuário: ID={}, Email={}", usuarioAtualizado.getId(), usuarioAtualizado.getEmail());
                    return usuarioAtualizado;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional(readOnly = true)
    public long contarUsuariosAtivos() {
        long count = usuarioRepository.countByAtivoTrue();
        log.debug("Total de usuários ativos: {}", count);
        return count;
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Long usuarioId) {
        boolean isAdmin = usuarioRepository.findById(usuarioId)
                .map(usuario -> usuario.getRoles().stream()
                        .anyMatch(role -> role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN")))
                .orElse(false);
        log.debug("Usuário ID {} é admin? {}", usuarioId, isAdmin);
        return isAdmin;
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNome(String nome) {
        log.debug("Buscando usuários por nome: {}", nome);
        return usuarioRepository.buscarPorNome(nome);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorRole(String role) {
        log.debug("Buscando usuários por role: {}", role);
        return usuarioRepository.findByRolesContains(role);
    }

    @Transactional
    public Usuario adicionarRole(Long id, String role) {
        log.debug("Adicionando role {} para usuário ID: {}", role, id);

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    List<String> roles = usuario.getRoles();
                    if (!roles.contains(role)) {
                        roles.add(role);
                        usuario.setRoles(roles);
                        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
                        log.info("Role {} adicionada para usuário: ID={}", role, usuarioAtualizado.getId());
                        return usuarioAtualizado;
                    }
                    log.debug("Usuário ID {} já possui a role {}", id, role);
                    return usuario;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public Usuario removerRole(Long id, String role) {
        log.debug("Removendo role {} do usuário ID: {}", role, id);

        return usuarioRepository.findById(id)
                .map(usuario -> {
                    List<String> roles = usuario.getRoles();
                    if (roles.contains(role)) {
                        roles.remove(role);
                        usuario.setRoles(roles);
                        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
                        log.info("Role {} removida do usuário: ID={}", role, usuarioAtualizado.getId());
                        return usuarioAtualizado;
                    }
                    log.debug("Usuário ID {} não possui a role {}", id, role);
                    return usuario;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }
}