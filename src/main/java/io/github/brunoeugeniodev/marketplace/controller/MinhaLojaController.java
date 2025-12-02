package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.*;
import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/minha-loja")
@RequiredArgsConstructor
public class MinhaLojaController {

    private final LojaService lojaService;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final MapperUtil mapperUtil;

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            boolean temLoja = !usuario.get().getLojas().isEmpty();

            Map<String, Object> response = new HashMap<>();
            response.put("temLoja", temLoja);
            if (temLoja) {
                Loja loja = usuario.get().getLojas().get(0);
                response.put("loja", mapperUtil.toLojaDTO(loja));
            }

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping
    public ResponseEntity<?> getMinhaLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);
            return ResponseEntity.ok(mapperUtil.toLojaDTO(loja));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Loja não encontrada"));
    }

    @PostMapping
    public ResponseEntity<?> criarOuAtualizarLoja(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LojaCreateDTO request) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                // Converter DTO para entidade
                Loja loja = mapperUtil.toLojaEntity(request);

                // Verificar se usuário já tem loja
                if (!usuario.get().getLojas().isEmpty()) {
                    // Atualizar loja existente
                    Loja lojaExistente = usuario.get().getLojas().get(0);
                    Loja lojaAtualizada = mapperUtil.toLojaEntity(request);
                    Loja lojaEditada = lojaService.atualizarLoja(lojaExistente.getId(), lojaAtualizada, usuario.get());
                    LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaEditada);
                    return ResponseEntity.ok(lojaDTO);
                } else {
                    // Criar nova loja
                    Loja lojaSalva = lojaService.criarLoja(loja, usuario.get());
                    LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaSalva);
                    return ResponseEntity.status(HttpStatus.CREATED).body(lojaDTO);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/produtos")
    public ResponseEntity<?> getProdutosDaLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);
            List<Produto> produtos = produtoService.listarProdutosPorLoja(loja.getId());
            List<ProdutoDTO> produtosDTO = mapperUtil.mapList(produtos, ProdutoDTO.class);
            return ResponseEntity.ok(produtosDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Loja não encontrada"));
    }

    @PostMapping("/produtos")
    public ResponseEntity<?> adicionarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProdutoCreateDTO request) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);

            try {
                // Converter DTO para entidade
                Produto produto = mapperUtil.toProdutoEntity(request);
                Produto produtoSalvo = produtoService.criarProduto(produto, loja, usuario.get());
                ProdutoDTO produtoDTO = mapperUtil.toProdutoDTO(produtoSalvo);
                return ResponseEntity.status(HttpStatus.CREATED).body(produtoDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuário não tem loja ou não tem permissão"));
    }

    @PutMapping("/produtos/{id}")
    public ResponseEntity<?> editarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ProdutoUpdateDTO request) {

        Optional<Produto> produtoExistente = produtoService.buscarPorId(id);
        if (produtoExistente.isPresent()) {
            Produto produto = produtoExistente.get();

            // Verificar se o usuário é dono da loja
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (usuario.isPresent() && produto.getLoja().getUsuario().getId().equals(usuario.get().getId())) {
                try {
                    // Converter DTO para entidade
                    Produto produtoAtualizado = mapperUtil.toProdutoEntity(request);
                    produtoAtualizado.setId(id); // Manter o mesmo ID

                    Produto produtoEditado = produtoService.atualizarProduto(id, produtoAtualizado, usuario.get());
                    ProdutoDTO produtoDTO = mapperUtil.toProdutoDTO(produtoEditado);
                    return ResponseEntity.ok(produtoDTO);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas o dono da loja pode editar produtos"));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/produtos/{id}/foto")
    public ResponseEntity<?> atualizarFotoProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String fotoUrl) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                Produto produtoAtualizado = produtoService.atualizarFotoProduto(id, fotoUrl, usuario.get());
                ProdutoDTO produtoDTO = mapperUtil.toProdutoDTO(produtoAtualizado);
                return ResponseEntity.ok(produtoDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/produtos/{id}/desativar")
    public ResponseEntity<?> desativarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                Produto produtoDesativado = produtoService.desativarProduto(id, usuario.get());
                ProdutoDTO produtoDTO = mapperUtil.toProdutoDTO(produtoDesativado);
                return ResponseEntity.ok(produtoDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<?> deletarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Produto> produto = produtoService.buscarPorId(id);
        if (produto.isPresent()) {
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (usuario.isPresent() && produto.get().getLoja().getUsuario().getId().equals(usuario.get().getId())) {
                try {
                    produtoService.deletarProduto(id, usuario.get());
                    return ResponseEntity.noContent().build();
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas o dono da loja pode deletar produtos"));
            }
        }
        return ResponseEntity.notFound().build();
    }
}