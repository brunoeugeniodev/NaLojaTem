package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.service.EnderecoService;
import io.github.brunoeugeniodev.marketplace.service.LojaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/lojas")
public class LojaController {

    private final LojaService lojaService;
    private final EnderecoService enderecoService;

    public LojaController(LojaService lojaService, EnderecoService enderecoService) {
        this.lojaService = lojaService;
        this.enderecoService = enderecoService;
    }

    @PostMapping
    public ResponseEntity<Loja> criarLoja(@RequestBody Loja loja){
        Loja lojaSalva = lojaService.salvarLoja(loja);
        return ResponseEntity.status(HttpStatus.CREATED).body(lojaSalva);
    }

    // Novo endpoint para cadastro via formulário
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarLojaViaForm(
            @RequestParam("nome") String nome,
            @RequestParam("cnpj") String cnpj,
            @RequestParam("descricao") String descricao,
            @RequestParam("rua") String rua,
            @RequestParam("bairro") String bairro,
            @RequestParam("cidade") String cidade,
            @RequestParam("numero") String numero,
            @RequestParam("estado") String estado,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Authentication authentication) {

        try {
            // 1. Cria o Endereco
            Endereco endereco = new Endereco();
            endereco.setRua(rua);
            endereco.setBairro(bairro);
            endereco.setCidade(cidade);
            endereco.setNumero(numero);
            endereco.setEstado(estado);

            // 2. Salva o endereço
            Endereco enderecoSalvo = enderecoService.salvarEndereco(endereco);

            // 3. Cria a Loja
            Loja loja = new Loja();
            loja.setNome(nome);
            loja.setCnpj(cnpj);
            loja.setDescricao(descricao);
            loja.setEndereco(enderecoSalvo);

            // 4. Associa a Loja ao Endereco (bidirecional)
            enderecoSalvo.setLoja(loja);
            enderecoService.salvarEndereco(enderecoSalvo);

            // 5. Processa upload da foto (se houver)
            if (foto != null && !foto.isEmpty()) {
                // String fotoUrl = fileStorageService.salvarFoto(foto);
                // loja.setFoto(fotoUrl);
                loja.setFoto("/imagens/lojas/placeholder.jpg"); // Placeholder
            }

            // 6. Salva a loja
            Loja lojaSalva = lojaService.salvarLoja(loja);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loja cadastrada com sucesso!");
            response.put("lojaId", lojaSalva.getId());
            response.put("lojaNome", lojaSalva.getNome());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao cadastrar loja: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Loja> editarLoja(@PathVariable Long id, @RequestBody Loja loja){
        Optional<Loja> optionalLoja = lojaService.editarLoja(id, loja);
        if(optionalLoja.isPresent()){
            return ResponseEntity.ok(optionalLoja.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para atualizar apenas a foto da loja
    @PutMapping("/{id}/foto")
    public ResponseEntity<?> atualizarFotoLoja(
            @PathVariable Long id,
            @RequestParam("foto") MultipartFile foto) {

        try {
            Optional<Loja> optionalLoja = lojaService.atualizarFoto(id, foto);
            if (optionalLoja.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Foto atualizada com sucesso!");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Erro ao atualizar foto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<Loja>> listarLojas(){
        List<Loja> lojas = lojaService.listarLojas();
        return ResponseEntity.ok(lojas);
    }

    // Endpoint para buscar lojas por nome (para busca)
    @GetMapping("/buscar")
    public ResponseEntity<List<Loja>> buscarLojas(@RequestParam String nome) {
        List<Loja> lojas = lojaService.buscarPorNome(nome);
        return ResponseEntity.ok(lojas);
    }

    // Endpoint para lojas recomendadas (para página inicial)
    @GetMapping("/recomendadas")
    public ResponseEntity<List<Loja>> listarRecomendadas(){
        List<Loja> lojas = lojaService.listarLojasRecomendadas();
        return ResponseEntity.ok(lojas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loja> listarPorId(@PathVariable Long id){
        Optional<Loja> optionalLoja = lojaService.listarLojaId(id);
        if(optionalLoja.isPresent()){
            return ResponseEntity.ok(optionalLoja.get());
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para produtos de uma loja específica
    @GetMapping("/{id}/produtos")
    public ResponseEntity<?> listarProdutosDaLoja(@PathVariable Long id) {
        Optional<Loja> optionalLoja = lojaService.listarLojaId(id);
        if (optionalLoja.isPresent()) {
            Loja loja = optionalLoja.get();
            return ResponseEntity.ok(loja.getProdutos());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLoja(@PathVariable Long id){
        boolean deletado = lojaService.removerLoja(id);
        if(deletado){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para buscar lojas do usuário logado
    @GetMapping("/minhas-lojas")
    public ResponseEntity<?> listarMinhasLojas(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuário não autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Aqui você buscaria as lojas do usuário logado
            // Por enquanto, retorna todas (implemente a lógica no service)
            List<Loja> lojas = lojaService.listarLojasPorUsuario(authentication.getName());
            return ResponseEntity.ok(lojas);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro ao buscar suas lojas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}