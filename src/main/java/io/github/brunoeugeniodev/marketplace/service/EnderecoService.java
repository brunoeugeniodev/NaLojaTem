package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.exception.ResourceNotFoundException;
import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.EnderecoRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;

    @Transactional
    public Endereco criarEndereco(Endereco endereco, Usuario usuario) {
        // Validações básicas
        validarEndereco(endereco);

        // Associa usuário
        endereco.setUsuario(usuario);

        // Se for o primeiro endereço ou marcado como principal, define como principal
        List<Endereco> enderecosExistentes = enderecoRepository.findByUsuarioId(usuario.getId());
        if (enderecosExistentes.isEmpty() || Boolean.TRUE.equals(endereco.getEnderecoPrincipal())) {
            if (!enderecosExistentes.isEmpty() && Boolean.TRUE.equals(endereco.getEnderecoPrincipal())) {
                // Remove principal dos outros endereços
                enderecoRepository.removerPrincipalDeTodos(usuario.getId());
            }
            endereco.setEnderecoPrincipal(true);
        }

        return enderecoRepository.save(endereco);
    }

    @Transactional
    public Endereco atualizarEndereco(Long id, Endereco enderecoAtualizado, Usuario usuario) {
        return enderecoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .map(endereco -> {
                    // Validações básicas
                    validarEndereco(enderecoAtualizado);

                    endereco.setRua(enderecoAtualizado.getRua());
                    endereco.setNumero(enderecoAtualizado.getNumero());
                    endereco.setBairro(enderecoAtualizado.getBairro());
                    endereco.setCidade(enderecoAtualizado.getCidade());
                    endereco.setEstado(enderecoAtualizado.getEstado());
                    endereco.setCep(enderecoAtualizado.getCep());
                    endereco.setComplemento(enderecoAtualizado.getComplemento());

                    // Se marcado como principal, atualiza outros endereços
                    if (Boolean.TRUE.equals(enderecoAtualizado.getEnderecoPrincipal()) &&
                            !Boolean.TRUE.equals(endereco.getEnderecoPrincipal())) {
                        enderecoRepository.removerPrincipalDeTodos(usuario.getId());
                        endereco.setEnderecoPrincipal(true);
                    }

                    return enderecoRepository.save(endereco);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
    }

    public List<Endereco> listarEnderecosDoUsuario(Usuario usuario) {
        return enderecoRepository.findByUsuarioIdOrdenado(usuario.getId());
    }

    public Optional<Endereco> buscarEnderecoPrincipal(Usuario usuario) {
        List<Endereco> principais = enderecoRepository.findByUsuarioIdAndEnderecoPrincipalTrue(usuario.getId());
        return principais.isEmpty() ? Optional.empty() : Optional.of(principais.get(0));
    }

    public Optional<Endereco> buscarPorIdEUsuario(Long id, Usuario usuario) {
        return enderecoRepository.findByIdAndUsuarioId(id, usuario.getId());
    }

    @Transactional
    public Endereco definirComoPrincipal(Long id, Usuario usuario) {
        Endereco endereco = enderecoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

        if (!Boolean.TRUE.equals(endereco.getEnderecoPrincipal())) {
            // Remove principal dos outros endereços
            enderecoRepository.removerPrincipalDeTodos(usuario.getId());

            // Define este como principal
            endereco.setEnderecoPrincipal(true);
            return enderecoRepository.save(endereco);
        }

        return endereco;
    }

    @Transactional
    public void deletarEndereco(Long id, Usuario usuario) {
        Endereco endereco = enderecoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

        // Verifica se é o endereço principal
        if (Boolean.TRUE.equals(endereco.getEnderecoPrincipal())) {
            throw new ValidationException("Não é possível deletar o endereço principal");
        }

        enderecoRepository.delete(endereco);
    }

    // Método auxiliar para validação
    private void validarEndereco(Endereco endereco) {
        if (endereco.getRua() == null || endereco.getRua().trim().isEmpty()) {
            throw new ValidationException("Rua é obrigatória");
        }

        if (endereco.getNumero() == null || endereco.getNumero().trim().isEmpty()) {
            throw new ValidationException("Número é obrigatório");
        }

        if (endereco.getBairro() == null || endereco.getBairro().trim().isEmpty()) {
            throw new ValidationException("Bairro é obrigatório");
        }

        if (endereco.getCidade() == null || endereco.getCidade().trim().isEmpty()) {
            throw new ValidationException("Cidade é obrigatória");
        }

        if (endereco.getEstado() == null || endereco.getEstado().trim().isEmpty()) {
            throw new ValidationException("Estado é obrigatório");
        }

        // Valida formato do estado (UF)
        if (endereco.getEstado() != null && endereco.getEstado().length() != 2) {
            throw new ValidationException("Estado deve ter 2 caracteres (UF)");
        }
    }
}