package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Loja> findByCnpj(String cnpj);

    List<Loja> findByNomeContainingIgnoreCase(String nome);

    List<Loja> findByAtivoTrue();

    List<Loja> findByUsuarioId(Long usuarioId);

    @Query("SELECT l FROM Loja l WHERE l.usuario.email = :email")
    List<Loja> findByUsuarioEmail(@Param("email") String email);

    @Query("SELECT l FROM Loja l WHERE LOWER(l.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
            "OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Loja> buscarPorTermo(@Param("termo") String termo);

    @Query("SELECT l FROM Loja l WHERE l.ativo = true " +
            "ORDER BY l.avaliacaoMedia DESC NULLS LAST, l.dataCriacao DESC")
    Page<Loja> findLojasRecomendadas(Pageable pageable);

    @Query("SELECT l FROM Loja l WHERE l.endereco.cidade = :cidade AND l.ativo = true")
    List<Loja> findByCidade(@Param("cidade") String cidade);

    @Query("SELECT COUNT(l) > 0 FROM Loja l WHERE l.cnpj = :cnpj AND l.id != :id")
    boolean existsByCnpjAndIdNot(@Param("cnpj") String cnpj, @Param("id") Long id);
}