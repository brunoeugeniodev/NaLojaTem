package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.CarrinhoService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/carrinho") // Mudei para /api/carrinho
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final UsuarioService usuarioService;

    public CarrinhoController(CarrinhoService carrinhoService, UsuarioService usuarioService) {
        this.carrinhoService = carrinhoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<Carrinho> getCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.obterCarrinho(usuario.get());
            return ResponseEntity.ok(carrinho);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/itens")
    public ResponseEntity<Carrinho> adicionarItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ItemRequest itemRequest) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.adicionarItem(
                    usuario.get(),
                    itemRequest.getProdutoId(),
                    itemRequest.getQuantidade()
            );
            return ResponseEntity.ok(carrinho);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<Carrinho> removerItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.removerItem(usuario.get(), itemId);
            return ResponseEntity.ok(carrinho);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/itens/{itemId}")
    public ResponseEntity<Carrinho> atualizarQuantidade(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantidade) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.atualizarQuantidade(
                    usuario.get(), itemId, quantidade
            );
            return ResponseEntity.ok(carrinho);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/limpar")
    public ResponseEntity<Carrinho> limparCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.limparCarrinho(usuario.get());
            return ResponseEntity.ok(carrinho);
        }
        return ResponseEntity.notFound().build();
    }

    public static class ItemRequest {
        private Long produtoId;
        private Integer quantidade;

        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }
}