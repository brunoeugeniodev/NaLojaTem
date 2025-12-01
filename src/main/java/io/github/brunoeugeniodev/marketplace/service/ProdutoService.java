package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // Método para obter produtos mais vendidos (por enquanto ordena por nome)
    public List<Produto> obterProdutosDestaque() {
        // TODO: Implementar lógica real de produtos mais vendidos
        // Por enquanto, retorna os primeiros 30 produtos ordenados por nome
        return produtoRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"))
                .stream()
                .limit(30)
                .toList();
    }

    public List<Produto> listarProdutosPorLoja(Long lojaId) {
        return produtoRepository.findByLojaId(lojaId);
    }

    // Métodos existentes...
    public Produto salvarProduto(Produto produto){
        return produtoRepository.save(produto);
    }

    public Optional<Produto> editarProduto(Long id, Produto produtoNovo){
        return produtoRepository.findById(id)
                .map(produtoExistente -> {
                    produtoExistente.setNome(produtoNovo.getNome());
                    produtoExistente.setDescricao(produtoNovo.getDescricao());
                    produtoExistente.setPreco(produtoNovo.getPreco());
                    produtoExistente.setQuantidade(produtoNovo.getQuantidade());
                    return produtoRepository.save(produtoExistente);
                });
    }

    public List<Produto> listarProdutos(){
        return produtoRepository.findAll();
    }

    public Optional<Produto> listarProdutoId(Long id){
        return produtoRepository.findById(id);
    }

    public boolean removerProduto(Long id){
        if(produtoRepository.existsById(id)){
            produtoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}