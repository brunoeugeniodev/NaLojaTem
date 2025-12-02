package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.CarrinhoDTO;
import io.github.brunoeugeniodev.marketplace.dto.ItemCarrinhoRequestDTO;
import io.github.brunoeugeniodev.marketplace.dto.ItemCarrinhoDTO;
import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.CarrinhoService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final UsuarioService usuarioService;
    private final MapperUtil mapperUtil;

    @GetMapping
    public ResponseEntity<CarrinhoDTO> getCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.obterCarrinhoCompleto(usuario.get());
            CarrinhoDTO carrinhoDTO = mapperUtil.mapCarrinhoToDTO(carrinho);
            return ResponseEntity.ok(carrinhoDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/itens")
    public ResponseEntity<CarrinhoDTO> adicionarItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ItemCarrinhoRequestDTO itemRequest) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.adicionarItem(
                    usuario.get(),
                    itemRequest.getProdutoId(),
                    itemRequest.getQuantidade()
            );
            CarrinhoDTO carrinhoDTO = mapperUtil.mapCarrinhoToDTO(carrinho);
            return ResponseEntity.ok(carrinhoDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<CarrinhoDTO> removerItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.removerItem(usuario.get(), itemId);
            CarrinhoDTO carrinhoDTO = mapperUtil.mapCarrinhoToDTO(carrinho);
            return ResponseEntity.ok(carrinhoDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/itens/{itemId}")
    public ResponseEntity<CarrinhoDTO> atualizarQuantidade(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantidade) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.atualizarQuantidade(
                    usuario.get(), itemId, quantidade
            );
            CarrinhoDTO carrinhoDTO = mapperUtil.mapCarrinhoToDTO(carrinho);
            return ResponseEntity.ok(carrinhoDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/limpar")
    public ResponseEntity<CarrinhoDTO> limparCarrinho(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Carrinho carrinho = carrinhoService.limparCarrinho(usuario.get());
            CarrinhoDTO carrinhoDTO = mapperUtil.mapCarrinhoToDTO(carrinho);
            return ResponseEntity.ok(carrinhoDTO);
        }
        return ResponseEntity.notFound().build();
    }
}