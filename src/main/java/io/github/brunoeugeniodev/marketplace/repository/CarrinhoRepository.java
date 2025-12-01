// CarrinhoRepository.java
package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    Optional<Carrinho> findByUsuario(Usuario usuario);
    Optional<Carrinho> findByUsuarioId(Long usuarioId);
}

