// CarrinhoService.java
package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.ItemCarrinho;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.repository.CarrinhoRepository;
import io.github.brunoeugeniodev.marketplace.repository.ItemCarrinhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ProdutoService produtoService;

    public CarrinhoService(CarrinhoRepository carrinhoRepository,
                           ItemCarrinhoRepository itemCarrinhoRepository,
                           ProdutoService produtoService) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.produtoService = produtoService;
    }

    public Carrinho obterCarrinho(Usuario usuario) {
        return carrinhoRepository.findByUsuario(usuario)
                .orElseGet(() -> criarCarrinho(usuario));
    }

    @Transactional
    public Carrinho adicionarItem(Usuario usuario, Long produtoId, Integer quantidade) {
        Carrinho carrinho = obterCarrinho(usuario);
        Optional<Produto> produto = produtoService.listarProdutoId(produtoId);

        if (produto.isPresent()) {
            Optional<ItemCarrinho> itemExistente = carrinho.getItens().stream()
                    .filter(item -> item.getProduto().getId().equals(produtoId))
                    .findFirst();

            if (itemExistente.isPresent()) {
                // Atualiza quantidade se item já existe
                ItemCarrinho item = itemExistente.get();
                item.setQuantidade(item.getQuantidade() + quantidade);
                itemCarrinhoRepository.save(item);
            } else {
                // Adiciona novo item
                ItemCarrinho novoItem = new ItemCarrinho();
                novoItem.setCarrinho(carrinho);
                novoItem.setProduto(produto.get());
                novoItem.setQuantidade(quantidade);
                carrinho.getItens().add(novoItem);
                itemCarrinhoRepository.save(novoItem);
            }

            return carrinhoRepository.save(carrinho);
        }

        throw new RuntimeException("Produto não encontrado");
    }

    @Transactional
    public Carrinho removerItem(Usuario usuario, Long itemId) {
        Carrinho carrinho = obterCarrinho(usuario);
        Optional<ItemCarrinho> item = itemCarrinhoRepository.findById(itemId);

        if (item.isPresent() && item.get().getCarrinho().getId().equals(carrinho.getId())) {
            carrinho.getItens().remove(item.get());
            itemCarrinhoRepository.delete(item.get());
            return carrinhoRepository.save(carrinho);
        }

        throw new RuntimeException("Item não encontrado no carrinho");
    }

    @Transactional
    public Carrinho atualizarQuantidade(Usuario usuario, Long itemId, Integer quantidade) {
        if (quantidade <= 0) {
            return removerItem(usuario, itemId);
        }

        Carrinho carrinho = obterCarrinho(usuario);
        Optional<ItemCarrinho> item = itemCarrinhoRepository.findById(itemId);

        if (item.isPresent() && item.get().getCarrinho().getId().equals(carrinho.getId())) {
            item.get().setQuantidade(quantidade);
            itemCarrinhoRepository.save(item.get());
            return carrinhoRepository.save(carrinho);
        }

        throw new RuntimeException("Item não encontrado no carrinho");
    }

    @Transactional
    public Carrinho limparCarrinho(Usuario usuario) {
        Carrinho carrinho = obterCarrinho(usuario);
        itemCarrinhoRepository.deleteAll(carrinho.getItens());
        carrinho.getItens().clear();
        return carrinhoRepository.save(carrinho);
    }

    private Carrinho criarCarrinho(Usuario usuario) {
        Carrinho carrinho = new Carrinho();
        carrinho.setUsuario(usuario);
        return carrinhoRepository.save(carrinho);
    }
}