package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.LojaCreateDTO;
import io.github.brunoeugeniodev.marketplace.dto.LojaDTO;
import io.github.brunoeugeniodev.marketplace.dto.ProdutoDTO;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/lojas")
@RequiredArgsConstructor
public class LojaController {

    private final LojaService lojaService;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final MapperUtil mapperUtil;

    @PostMapping
    public ResponseEntity<?> criarLoja(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @Valid @RequestBody LojaCreateDTO lojaCreateDTO) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                // Converter DTO para entidade
                Loja loja = mapperUtil.toLojaEntity(lojaCreateDTO);
                Loja lojaSalva = lojaService.criarLoja(loja, usuario.get());
                LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaSalva);
                return ResponseEntity.status(HttpStatus.CREATED).body(lojaDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping
    public ResponseEntity<List<LojaDTO>> listarLojas() {
        List<Loja> lojas = lojaService.listarLojasAtivas();
        List<LojaDTO> lojasDTO = mapperUtil.mapList(lojas, LojaDTO.class);
        return ResponseEntity.ok(lojasDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LojaDTO> listarPorId(@PathVariable Long id) {
        Optional<Loja> optionalLoja = lojaService.buscarPorIdAtiva(id);
        if (optionalLoja.isPresent()) {
            LojaDTO lojaDTO = mapperUtil.toLojaDTO(optionalLoja.get());
            return ResponseEntity.ok(lojaDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/produtos")
    public ResponseEntity<List<ProdutoDTO>> listarProdutosDaLoja(@PathVariable Long id) {
        List<Produto> produtos = produtoService.listarProdutosPorLoja(id);
        List<ProdutoDTO> produtosDTO = mapperUtil.mapList(produtos, ProdutoDTO.class);
        return ResponseEntity.ok(produtosDTO);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LojaDTO>> buscarLojas(@RequestParam String nome) {
        List<Loja> lojas = lojaService.buscarPorNome(nome);
        List<LojaDTO> lojasDTO = mapperUtil.mapList(lojas, LojaDTO.class);
        return ResponseEntity.ok(lojasDTO);
    }

    @GetMapping("/recomendadas")
    public ResponseEntity<List<LojaDTO>> listarRecomendadas() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Loja> lojas = lojaService.listarLojasRecomendadas(pageable).getContent();
        List<LojaDTO> lojasDTO = mapperUtil.mapList(lojas, LojaDTO.class);
        return ResponseEntity.ok(lojasDTO);
    }

    @GetMapping("/minhas-lojas")
    public ResponseEntity<List<LojaDTO>> listarMinhasLojas(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<Usuario> usuario = usuarioService.buscarPorEmail(authentication.getName());
            if (usuario.isPresent()) {
                List<Loja> lojas = lojaService.listarLojasDoUsuario(usuario.get());
                List<LojaDTO> lojasDTO = mapperUtil.mapList(lojas, LojaDTO.class);
                return ResponseEntity.ok(lojasDTO);
            }
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Erro ao buscar lojas do usuário", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarLoja(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LojaCreateDTO request) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                Loja lojaAtualizada = mapperUtil.toLojaEntity(request);
                Loja lojaEditada = lojaService.atualizarLoja(id, lojaAtualizada, usuario.get());
                LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaEditada);
                return ResponseEntity.ok(lojaDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}/foto")
    public ResponseEntity<?> atualizarFotoLoja(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String fotoUrl) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                Loja lojaAtualizada = lojaService.atualizarFotoLoja(id, fotoUrl, usuario.get());
                LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaAtualizada);
                return ResponseEntity.ok(lojaDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}/desativar")
    public ResponseEntity<?> desativarLoja(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                Loja lojaDesativada = lojaService.desativarLoja(id, usuario.get());
                LojaDTO lojaDTO = mapperUtil.toLojaDTO(lojaDesativada);
                return ResponseEntity.ok(lojaDTO);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLoja(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            try {
                lojaService.deletarLoja(id, usuario.get());
                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                log.error("Erro ao deletar loja: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}