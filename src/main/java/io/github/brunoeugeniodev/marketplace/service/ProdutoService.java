package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.exception.ResourceNotFoundException;
import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.ProdutoRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public Produto criarProduto(Produto produto, Loja loja, Usuario usuario) {
        // Verifica se usuário é dono da loja
        if (!loja.getUsuario().getId().equals(usuario.getId())) {
            throw new ValidationException("Você não tem permissão para adicionar produtos a esta loja");
        }

        // Validações
        validarProduto(produto);

        // Associa loja
        produto.setLoja(loja);
        produto.setAtivo(true);
        produto.setTotalVendas(0);

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizarProduto(Long id, Produto produtoAtualizado, Usuario usuario) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    // Verifica se usuário é dono da loja
                    if (!produto.getLoja().getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para editar este produto");
                    }

                    // Validações
                    validarProduto(produtoAtualizado);

                    produto.setNome(produtoAtualizado.getNome());
                    produto.setDescricao(produtoAtualizado.getDescricao());
                    produto.setPreco(produtoAtualizado.getPreco());
                    produto.setQuantidade(produtoAtualizado.getQuantidade());
                    produto.setFotoUrl(produtoAtualizado.getFotoUrl());
                    produto.setCategoria(produtoAtualizado.getCategoria());
                    produto.setMarca(produtoAtualizado.getMarca());
                    produto.setModelo(produtoAtualizado.getModelo());
                    produto.setDestaque(produtoAtualizado.getDestaque());

                    return produtoRepository.save(produto);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Transactional
    public Produto atualizarFotoProduto(Long id, String fotoUrl, Usuario usuario) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    // Verifica se usuário é dono da loja
                    if (!produto.getLoja().getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para editar este produto");
                    }

                    produto.setFotoUrl(fotoUrl);
                    return produtoRepository.save(produto);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    public List<Produto> listarProdutosAtivos() {
        return produtoRepository.findProdutosAtivos();
    }

    public Page<Produto> listarProdutosDestaque(Pageable pageable) {
        return produtoRepository.findProdutosDestaque(pageable);
    }

    public List<Produto> buscarPorTermo(String termo) {
        return produtoRepository.buscarPorTermo(termo);
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.buscarPorNome(nome);
    }

    public List<Produto> listarProdutosPorLoja(Long lojaId) {
        return produtoRepository.findByLojaIdAndAtivoTrue(lojaId);
    }

    public Page<Produto> listarMaisVendidosPorLoja(Long lojaId, Pageable pageable) {
        return produtoRepository.findMaisVendidosPorLoja(lojaId, pageable);
    }

    public Optional<Produto> buscarProdutoAtivoPorId(Long id) {
        return produtoRepository.findById(id)
                .filter(Produto::getAtivo)
                .filter(p -> p.getLoja() != null && Boolean.TRUE.equals(p.getLoja().getAtivo()));
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Transactional
    public Produto desativarProduto(Long id, Usuario usuario) {
        return produtoRepository.findById(id)
                .map(produto -> {
                    // Verifica se usuário é dono da loja
                    if (!produto.getLoja().getUsuario().getId().equals(usuario.getId())) {
                        throw new ValidationException("Você não tem permissão para desativar este produto");
                    }

                    produto.setAtivo(false);
                    return produtoRepository.save(produto);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Transactional
    public void deletarProduto(Long id, Usuario usuario) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        // Verifica se usuário é dono da loja
        if (!produto.getLoja().getUsuario().getId().equals(usuario.getId())) {
            throw new ValidationException("Você não tem permissão para deletar este produto");
        }

        // Verifica se produto tem vendas
        if (produto.getTotalVendas() > 0) {
            throw new ValidationException("Não é possível deletar um produto com vendas registradas");
        }

        produtoRepository.delete(produto);
    }

    @Transactional
    public Produto atualizarEstoque(Long produtoId, Long quantidadeVendida) {
        return produtoRepository.findById(produtoId)
                .map(produto -> {
                    if (produto.getQuantidade() >= quantidadeVendida) {
                        produto.setQuantidade(produto.getQuantidade() - quantidadeVendida);
                        produto.setTotalVendas(produto.getTotalVendas() + quantidadeVendida.intValue());
                        return produtoRepository.save(produto);
                    } else {
                        throw new ValidationException("Estoque insuficiente para o produto: " + produto.getNome());
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    public Long contarProdutosAtivosPorLoja(Long lojaId) {
        return produtoRepository.countProdutosAtivosPorLoja(lojaId);
    }

    // Método auxiliar para validação
    private void validarProduto(Produto produto) {
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new ValidationException("Nome do produto é obrigatório");
        }

        if (produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Preço deve ser maior que zero");
        }

        if (produto.getQuantidade() == null || produto.getQuantidade() < 0) {
            throw new ValidationException("Quantidade não pode ser negativa");
        }

        // Valida tamanhos máximos
        if (produto.getNome() != null && produto.getNome().length() > 200) {
            throw new ValidationException("Nome deve ter no máximo 200 caracteres");
        }

        if (produto.getDescricao() != null && produto.getDescricao().length() > 1000) {
            throw new ValidationException("Descrição deve ter no máximo 1000 caracteres");
        }

        if (produto.getFotoUrl() != null && produto.getFotoUrl().length() > 500) {
            throw new ValidationException("URL da foto deve ter no máximo 500 caracteres");
        }

        if (produto.getCategoria() != null && produto.getCategoria().length() > 100) {
            throw new ValidationException("Categoria deve ter no máximo 100 caracteres");
        }

        if (produto.getMarca() != null && produto.getMarca().length() > 100) {
            throw new ValidationException("Marca deve ter no máximo 100 caracteres");
        }

        if (produto.getModelo() != null && produto.getModelo().length() > 100) {
            throw new ValidationException("Modelo deve ter no máximo 100 caracteres");
        }
    }
}