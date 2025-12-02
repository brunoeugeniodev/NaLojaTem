package io.github.brunoeugeniodev.marketplace.controller;

import io.github.brunoeugeniodev.marketplace.dto.EnderecoCreateDTO;
import io.github.brunoeugeniodev.marketplace.dto.EnderecoDTO;
import io.github.brunoeugeniodev.marketplace.dto.EnderecoUpdateDTO;
import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import io.github.brunoeugeniodev.marketplace.service.EnderecoService;
import io.github.brunoeugeniodev.marketplace.service.UsuarioService;
import io.github.brunoeugeniodev.marketplace.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;
    private final UsuarioService usuarioService;
    private final MapperUtil mapperUtil;

    @PostMapping
    public ResponseEntity<EnderecoDTO> criarEndereco(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EnderecoCreateDTO enderecoCreateDTO) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Endereco endereco = mapperUtil.toEnderecoEntity(enderecoCreateDTO);
            Endereco enderecoSalvo = enderecoService.criarEndereco(endereco, usuario.get());
            EnderecoDTO enderecoDTO = mapperUtil.toEnderecoDTO(enderecoSalvo);
            return ResponseEntity.status(HttpStatus.CREATED).body(enderecoDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnderecoDTO> editarEndereco(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EnderecoUpdateDTO enderecoUpdateDTO) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Endereco enderecoAtualizado = mapperUtil.toEnderecoEntity(enderecoUpdateDTO);
            Endereco enderecoEditado = enderecoService.atualizarEndereco(id, enderecoAtualizado, usuario.get());
            EnderecoDTO enderecoDTO = mapperUtil.toEnderecoDTO(enderecoEditado);
            return ResponseEntity.ok(enderecoDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping
    public ResponseEntity<List<EnderecoDTO>> listarEnderecosDoUsuario(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            List<Endereco> enderecos = enderecoService.listarEnderecosDoUsuario(usuario.get());
            List<EnderecoDTO> enderecosDTO = enderecos.stream()
                    .map(mapperUtil::toEnderecoDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(enderecosDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoDTO> buscarEnderecoPorId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            Optional<Endereco> endereco = enderecoService.buscarPorIdEUsuario(id, usuario.get());
            if (endereco.isPresent()) {
                EnderecoDTO enderecoDTO = mapperUtil.toEnderecoDTO(endereco.get());
                return ResponseEntity.ok(enderecoDTO);
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/{id}/principal")
    public ResponseEntity<Void> definirComoPrincipal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            enderecoService.definirComoPrincipal(id, usuario.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEndereco(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.buscarPorEmail(userDetails.getUsername());
        if (usuario.isPresent()) {
            enderecoService.deletarEndereco(id, usuario.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}