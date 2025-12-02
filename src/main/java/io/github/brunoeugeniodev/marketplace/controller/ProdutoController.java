package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.ProdutoDTO;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarProdutos() {
        List<Produto> produtos = produtoService.listarProdutosAtivos();
        List<ProdutoDTO> produtosDTO = mapperUtil.mapList(produtos, ProdutoDTO.class);
        return ResponseEntity.ok(produtosDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> listarPorId(@PathVariable Long id) {
        Optional<Produto> optionalProduto = produtoService.buscarProdutoAtivoPorId(id);
        if (optionalProduto.isPresent()) {
            ProdutoDTO produtoDTO = mapperUtil.toProdutoDTO(optionalProduto.get());
            return ResponseEntity.ok(produtoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/loja/{lojaId}")
    public ResponseEntity<List<ProdutoDTO>> listarProdutosDaLoja(@PathVariable Long lojaId) {
        List<Produto> produtos = produtoService.listarProdutosPorLoja(lojaId);
        List<ProdutoDTO> produtosDTO = mapperUtil.mapList(produtos, ProdutoDTO.class);
        return ResponseEntity.ok(produtosDTO);
    }
}