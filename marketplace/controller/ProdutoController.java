package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final LojaService lojaService;

    public ProdutoController(ProdutoService produtoService, LojaService lojaService) {
        this.produtoService = produtoService;
        this.lojaService = lojaService;
    }

    // Endpoint público para listar produtos de uma loja específica
    @GetMapping("/loja/{lojaId}")
    public ResponseEntity<List<Produto>> listarProdutosDaLoja(@PathVariable Long lojaId) {
        Optional<Loja> loja = lojaService.listarLojaId(lojaId);
        if (loja.isPresent()) {
            return ResponseEntity.ok(loja.get().getProdutos());
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoints públicos (qualquer um pode acessar)
    @GetMapping
    public ResponseEntity<List<Produto>> listarProdutos(){
        List<Produto> produtos = produtoService.listarProdutos();
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> listarPorId(@PathVariable Long id){
        Optional<Produto> optionalProduto = produtoService.listarProdutoId(id);
        if(optionalProduto.isPresent()){
            return ResponseEntity.ok(optionalProduto.get());
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}