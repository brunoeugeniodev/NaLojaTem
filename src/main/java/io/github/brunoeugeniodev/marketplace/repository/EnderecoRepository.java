package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByUsuarioId(Long usuarioId);

    List<Endereco> findByUsuarioIdAndEnderecoPrincipalTrue(Long usuarioId);

    Optional<Endereco> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Query("SELECT e FROM Endereco e WHERE e.usuario.email = :email")
    List<Endereco> findByUsuarioEmail(@Param("email") String email);

    @Query("SELECT e FROM Endereco e WHERE e.usuario.id = :usuarioId " +
            "ORDER BY e.enderecoPrincipal DESC, e.id DESC")
    List<Endereco> findByUsuarioIdOrdenado(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE Endereco e SET e.enderecoPrincipal = false WHERE e.usuario.id = :usuarioId")
    void removerPrincipalDeTodos(@Param("usuarioId") Long usuarioId);
}