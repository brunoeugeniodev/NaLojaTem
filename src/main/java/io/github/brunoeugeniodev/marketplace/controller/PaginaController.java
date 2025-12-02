package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.models.Produto;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import io.github.brunoeugeniodev.marketplace.service.ProdutoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class PaginaController {

    private final LojaService lojaService;
    private final ProdutoService produtoService;

    public PaginaController(LojaService lojaService, ProdutoService produtoService) {
        this.lojaService = lojaService;
        this.produtoService = produtoService;
    }

    // Método auxiliar para injetar o status de login
    private void addLoginStatus(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("usuarioLogado", userDetails.getUsername());
        } else {
            model.addAttribute("usuarioLogado", null);
        }
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        List<Loja> lojas = lojaService.listarLojasAtivas();
        List<Produto> produtosDestaque = produtoService.listarProdutosAtivos();

        model.addAttribute("lojas", lojas);
        model.addAttribute("produtosDestaque", produtosDestaque);
        addLoginStatus(model); // Adiciona o status de login
        return "index";
    }

    // Novo endpoint para a página de resultados da busca
    @GetMapping("/busca-resultado")
    public String buscaResultado(@RequestParam(required = false) String q, Model model) {
        // Implementação simplificada: no frontend JS a busca real é via API /api/busca
        // Esta página seria um placeholder para exibir os resultados da API via AJAX
        model.addAttribute("termoBusca", q);
        addLoginStatus(model);
        return "busca-resultado"; // Crie este arquivo se necessário
    }

    @GetMapping("/lojas")
    public String lojas(Model model) {
        List<Loja> lojas = lojaService.listarLojasAtivas();
        model.addAttribute("lojas", lojas);
        addLoginStatus(model);
        return "lojas";
    }

    @GetMapping("/loja/{id}")
    public String loja(@PathVariable Long id, Model model) {
        Optional<Loja> optionalLoja = lojaService.buscarPorIdAtiva(id);
        if (optionalLoja.isPresent()) {
            Loja loja = optionalLoja.get();
            List<Produto> produtos = produtoService.listarProdutosPorLoja(id);
            model.addAttribute("loja", loja);
            model.addAttribute("produtos", produtos);
        } else {
            model.addAttribute("loja", null);
            model.addAttribute("produtos", Collections.emptyList());
        }
        addLoginStatus(model);
        return "loja";
    }

    @GetMapping("/ofertas")
    public String ofertas(Model model) {
        addLoginStatus(model);
        return "ofertas";
    }

    @GetMapping("/lancamentos")
    public String lancamentos(Model model) {
        addLoginStatus(model);
        return "lancamentos";
    }

    @GetMapping("/destaques")
    public String destaques(Model model) {
        List<Produto> produtosDestaque = produtoService.listarProdutosAtivos();
        model.addAttribute("produtosDestaque", produtosDestaque);
        addLoginStatus(model);
        return "destaques";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        addLoginStatus(model);
        return "registro";
    }

    @GetMapping("/minha-loja")
    public String minhaLoja(HttpSession session, Model model) {
        // A segurança deve ser tratada pelo SecurityConfig para esta rota
        // O PaginaController apenas renderiza
        addLoginStatus(model);
        return "minha-loja";
    }

    @GetMapping("/cadastro-loja")
    public String cadastroLoja(HttpSession session, Model model) {
        addLoginStatus(model);
        return "cadastro-loja";
    }

    @GetMapping("/carrinho")
    public String carrinho(HttpSession session, Model model) {
        // A segurança deve ser tratada pelo SecurityConfig para esta rota
        addLoginStatus(model);
        return "carrinho";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        addLoginStatus(model);
        return "login";
    }
}