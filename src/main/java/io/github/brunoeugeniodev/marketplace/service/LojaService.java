package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.exception.ResourceNotFoundException;
import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.LojaRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LojaService {

    private final LojaRepository lojaRepository;
    private final ProdutoService produtoService;

    @Transactional
    public Loja criarLoja(Loja loja, Usuario usuario) {
        // Validações
        validarLoja(loja);

        // Associa usuário
        loja.setUsuario(usuario);
        loja.setAtivo(true);

        return lojaRepository.save(loja);
    }

    @Transactional
    public Loja atualizarLoja(Long id, Loja lojaAtualizada, Usuario usuario) {
        return lojaRepository.findById(id)
                .map(loja -> {
                    // Verifica se usuário é dono da loja
                    if (!loja.getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para editar esta loja");
                    }

                    // Validações
                    validarLoja(lojaAtualizada);

                    // Valida CNPJ se foi alterado
                    if (!loja.getCnpj().equals(lojaAtualizada.getCnpj()) &&
                            lojaRepository.existsByCnpjAndIdNot(lojaAtualizada.getCnpj(), id)) {
                        throw new ValidationException("CNPJ já está em uso");
                    }

                    // Atualiza campos
                    loja.setNome(lojaAtualizada.getNome());
                    loja.setCnpj(lojaAtualizada.getCnpj());
                    loja.setDescricao(lojaAtualizada.getDescricao());
                    loja.setTelefone(lojaAtualizada.getTelefone());
                    loja.setEmail(lojaAtualizada.getEmail());
                    loja.setSite(lojaAtualizada.getSite());

                    // Atualiza endereço se fornecido
                    if (lojaAtualizada.getEndereco() != null) {
                        loja.setEndereco(lojaAtualizada.getEndereco());
                    }

                    return lojaRepository.save(loja);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    @Transactional
    public Loja atualizarFotoLoja(Long id, String fotoUrl, Usuario usuario) {
        return lojaRepository.findById(id)
                .map(loja -> {
                    // Verifica se usuário é dono da loja
                    if (!loja.getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para editar esta loja");
                    }

                    loja.setFotoUrl(fotoUrl);
                    return lojaRepository.save(loja);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    public List<Loja> listarLojasAtivas() {
        return lojaRepository.findByAtivoTrue();
    }

    public Page<Loja> listarLojasRecomendadas(Pageable pageable) {
        return lojaRepository.findLojasRecomendadas(pageable);
    }

    public Optional<Loja> buscarPorId(Long id) {
        return lojaRepository.findById(id);
    }

    public Optional<Loja> buscarPorIdAtiva(Long id) {
        return lojaRepository.findById(id)
                .filter(Loja::getAtivo);
    }

    public List<Loja> buscarPorNome(String nome) {
        return lojaRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Loja> buscarPorTermo(String termo) {
        return lojaRepository.buscarPorTermo(termo);
    }

    public List<Loja> listarLojasDoUsuario(Usuario usuario) {
        return lojaRepository.findByUsuarioId(usuario.getId());
    }

    public List<Loja> listarLojasPorUsuarioEmail(String email) {
        return lojaRepository.findByUsuarioEmail(email);
    }

    @Transactional
    public Loja desativarLoja(Long id, Usuario usuario) {
        return lojaRepository.findById(id)
                .map(loja -> {
                    if (!loja.getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para desativar esta loja");
                    }
                    loja.setAtivo(false);
                    return lojaRepository.save(loja);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    @Transactional
    public void deletarLoja(Long id, Usuario usuario) {
        Loja loja = lojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));

        // Verifica se usuário é dono da loja
        if (!loja.getUsuario().getId().equals(usuario.getId())) {
            throw new ValidationException("Você não tem permissão para deletar esta loja");
        }

        // Verifica se loja tem produtos
        if (!loja.getProdutos().isEmpty()) {
            throw new ValidationException("Não é possível deletar uma loja com produtos cadastrados");
        }

        lojaRepository.delete(loja);
    }

    public Long contarProdutosAtivosPorLoja(Long lojaId) {
        return produtoService.contarProdutosAtivosPorLoja(lojaId);
    }

    // Método auxiliar para validação
    private void validarLoja(Loja loja) {
        if (loja.getNome() == null || loja.getNome().trim().isEmpty()) {
            throw new ValidationException("Nome da loja é obrigatório");
        }

        if (loja.getCnpj() == null || loja.getCnpj().trim().isEmpty()) {
            throw new ValidationException("CNPJ é obrigatório");
        }

        // Valida CNPJ
        if (!loja.getCnpj().matches("\\d{14}")) {
            throw new ValidationException("CNPJ deve ter 14 dígitos");
        }

        // Verifica se CNPJ já existe
        if (lojaRepository.existsByCnpj(loja.getCnpj())) {
            throw new ValidationException("CNPJ já cadastrado");
        }

        // Valida tamanhos máximos
        if (loja.getNome() != null && loja.getNome().length() > 100) {
            throw new ValidationException("Nome deve ter no máximo 100 caracteres");
        }

        if (loja.getDescricao() != null && loja.getDescricao().length() > 500) {
            throw new ValidationException("Descrição deve ter no máximo 500 caracteres");
        }

        if (loja.getTelefone() != null && loja.getTelefone().length() > 20) {
            throw new ValidationException("Telefone deve ter no máximo 20 caracteres");
        }

        if (loja.getEmail() != null && loja.getEmail().length() > 100) {
            throw new ValidationException("Email deve ter no máximo 100 caracteres");
        }

        if (loja.getSite() != null && loja.getSite().length() > 200) {
            throw new ValidationException("Site deve ter no máximo 200 caracteres");
        }

        if (loja.getFotoUrl() != null && loja.getFotoUrl().length() > 500) {
            throw new ValidationException("URL da foto deve ter no máximo 500 caracteres");
        }
    }
}