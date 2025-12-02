package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.SearchResultDTO;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/busca")
@RequiredArgsConstructor
public class BuscaController {

    private final LojaService lojaService;
    private final ProdutoService produtoService;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<SearchResultDTO> buscar(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Realizando busca por: {}", q);

        SearchResultDTO resultado = SearchResultDTO.builder()
                .lojas(mapperUtil.mapList(lojaService.buscarPorTermo(q), io.github.brunoeugeniodev.marketplace.dto.LojaDTO.class))
                .produtos(mapperUtil.mapList(produtoService.buscarPorTermo(q), io.github.brunoeugeniodev.marketplace.dto.ProdutoDTO.class))
                .build();

        return ResponseEntity.ok(resultado);
    }
}