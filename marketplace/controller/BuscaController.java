package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/busca")
public class BuscaController {

    private final LojaService lojaService;
    private final ProdutoService produtoService;

    public BuscaController(LojaService lojaService, ProdutoService produtoService) {
        this.lojaService = lojaService;
        this.produtoService = produtoService;
    }

    @GetMapping
    public Map<String, Object> buscar(@RequestParam String q) {
        Map<String, Object> resultados = new HashMap<>();

        // Buscar lojas
        List<Loja> todasLojas = lojaService.listarLojas();
        List<Loja> lojasFiltradas = todasLojas.stream()
                .filter(loja -> loja.getNome().toLowerCase().contains(q.toLowerCase()))
                .collect(Collectors.toList());

        // Buscar produtos
        List<Produto> todosProdutos = produtoService.listarProdutos();
        List<Produto> produtosFiltrados = todosProdutos.stream()
                .filter(produto -> produto.getNome().toLowerCase().contains(q.toLowerCase()))
                .collect(Collectors.toList());

        resultados.put("lojas", lojasFiltradas);
        resultados.put("produtos", produtosFiltrados);

        return resultados;
    }
}