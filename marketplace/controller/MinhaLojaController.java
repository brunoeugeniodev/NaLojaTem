package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/minha-loja")
public class MinhaLojaController {

    private final LojaService lojaService;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    public MinhaLojaController(LojaService lojaService, UsuarioService usuarioService, ProdutoService produtoService) {
        this.lojaService = lojaService;
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
    }

    // Verificar se usuário tem loja
    @GetMapping("/verificar")
    public ResponseEntity<?> verificarLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            boolean temLoja = !usuario.get().getLojas().isEmpty();

            Map<String, Object> response = new HashMap<>();
            response.put("temLoja", temLoja);
            if (temLoja) {
                response.put("loja", usuario.get().getLojas().get(0));
            }

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Obter dados da loja do usuário
    @GetMapping
    public ResponseEntity<?> getMinhaLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);
            return ResponseEntity.ok(loja);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loja não encontrada");
    }

    // Criar ou atualizar loja do usuário
    @PostMapping
    public ResponseEntity<?> criarOuAtualizarLoja(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LojaRequest request) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Loja loja;

            // Verificar se usuário já tem loja
            if (!usuario.get().getLojas().isEmpty()) {
                // Atualizar loja existente
                loja = usuario.get().getLojas().get(0);
                loja.setNome(request.getNome());
                loja.setCnpj(request.getCnpj());
                loja.setDescricao(request.getDescricao());
                loja.setFoto(request.getFoto());
            } else {
                // Criar nova loja
                loja = new Loja();
                loja.setUsuario(usuario.get());
                loja.setNome(request.getNome());
                loja.setCnpj(request.getCnpj());
                loja.setDescricao(request.getDescricao());
                loja.setFoto(request.getFoto());
            }

            Loja lojaSalva = lojaService.salvarLoja(loja);
            return ResponseEntity.ok(lojaSalva);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // GESTÃO DE PRODUTOS DA LOJA

    // Obter produtos da loja do usuário
    @GetMapping("/produtos")
    public ResponseEntity<?> getProdutosDaLoja(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);
            return ResponseEntity.ok(loja.getProdutos());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Loja não encontrada");
    }

    // Adicionar produto à loja
    @PostMapping("/produtos")
    public ResponseEntity<?> adicionarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProdutoRequest request) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent() && !usuario.get().getLojas().isEmpty()) {
            Loja loja = usuario.get().getLojas().get(0);

            Produto produto = new Produto();
            produto.setLoja(loja);
            produto.setNome(request.getNome());
            produto.setDescricao(request.getDescricao());
            produto.setPreco(request.getPreco());
            produto.setQuantidade(request.getQuantidade());

            Produto produtoSalvo = produtoService.salvarProduto(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoSalvo);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário não tem permissão para adicionar produtos");
    }

    // Editar produto da loja (apenas dono)
    @PutMapping("/produtos/{id}")
    public ResponseEntity<?> editarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody ProdutoRequest request) {

        Optional<Produto> produtoExistente = produtoService.listarProdutoId(id);
        if (produtoExistente.isPresent()) {
            Produto produto = produtoExistente.get();

            // Verificar se o usuário é dono da loja
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (usuario.isPresent() && produto.getLoja().getUsuario().getId().equals(usuario.get().getId())) {
                produto.setNome(request.getNome());
                produto.setDescricao(request.getDescricao());
                produto.setPreco(request.getPreco());
                produto.setQuantidade(request.getQuantidade());

                Produto produtoEditado = produtoService.salvarProduto(produto);
                return ResponseEntity.ok(produtoEditado);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o dono da loja pode editar produtos");
            }
        }
        return ResponseEntity.notFound().build();
    }

    // Deletar produto da loja (apenas dono)
    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<?> deletarProduto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Produto> produto = produtoService.listarProdutoId(id);
        if (produto.isPresent()) {
            // Verificar se o usuário é dono da loja
            Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (usuario.isPresent() && produto.get().getLoja().getUsuario().getId().equals(usuario.get().getId())) {
                boolean deletado = produtoService.removerProduto(id);
                if (deletado) {
                    return ResponseEntity.noContent().build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o dono da loja pode deletar produtos");
            }
        }
        return ResponseEntity.notFound().build();
    }

    // Classes para requests

    public static class LojaRequest {
        private String nome;
        private String cnpj;
        private String descricao;
        private String foto;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getCnpj() { return cnpj; }
        public void setCnpj(String cnpj) { this.cnpj = cnpj; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public String getFoto() { return foto; }
        public void setFoto(String foto) { this.foto = foto; }
    }

    public static class ProdutoRequest {
        private String nome;
        private String descricao;
        private BigDecimal preco;
        private Long quantidade;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public BigDecimal getPreco() { return preco; }
        public void setPreco(BigDecimal preco) { this.preco = preco; }
        public Long getQuantidade() { return quantidade; }
        public void setQuantidade(Long quantidade) { this.quantidade = quantidade; }
    }
}