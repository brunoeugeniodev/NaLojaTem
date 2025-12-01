package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.service.EnderecoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @PostMapping
    public ResponseEntity<Endereco> criarEndereco(@RequestBody Endereco endereco){
        Endereco enderecoSalvo = enderecoService.salvarEndereco(endereco);
        return ResponseEntity.status(HttpStatus.CREATED).body(enderecoSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endereco> editarEndereco(@PathVariable Long id, @RequestBody Endereco endereco){
        Optional<Endereco> optionalEndereco = enderecoService.editarEndereco(id, endereco);
        if(optionalEndereco.isPresent()){
            return ResponseEntity.ok(optionalEndereco.get());
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Endereco>> listarEnderecos(){
        List<Endereco> enderecos = enderecoService.listarEnderecos();
        return ResponseEntity.ok(enderecos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> listarPorId(@PathVariable Long id){
        Optional<Endereco> optionalEndereco = enderecoService.listarEnderecoId(id);
        if(optionalEndereco.isPresent()){
            return ResponseEntity.ok(optionalEndereco.get());
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEndereco(@PathVariable Long id){
        boolean deletado = enderecoService.removerEndereco(id);
        if(deletado){
            return ResponseEntity.noContent().build();
        }else {
            return ResponseEntity.notFound().build();
        }
    }
}