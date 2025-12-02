package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Carrinho;
import io.github.brunoeugeniodev.marketplace.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuario(Usuario usuario);

    Optional<Carrinho> findByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM Carrinho c LEFT JOIN FETCH c.itens i LEFT JOIN FETCH i.produto " +
            "WHERE c.usuario.id = :usuarioId")
    Optional<Carrinho> findByUsuarioIdComItens(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(c) > 0 FROM Carrinho c WHERE c.usuario.id = :usuarioId")
    boolean existsByUsuarioId(@Param("usuarioId") Long usuarioId);
}