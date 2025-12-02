package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByLojaId(Long lojaId);

    List<Produto> findByLojaIdAndAtivoTrue(Long lojaId);

    List<Produto> findByCategoria(String categoria);

    List<Produto> findByDestaqueTrueAndAtivoTrue();

    List<Produto> findByAtivoTrue();

    @Query("SELECT p FROM Produto p WHERE p.loja.ativo = true AND p.ativo = true")
    List<Produto> findProdutosAtivos();

    @Query("SELECT p FROM Produto p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
            "AND p.ativo = true")
    List<Produto> buscarPorNome(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p WHERE " +
            "(LOWER(p.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
            "AND p.ativo = true")
    List<Produto> buscarPorTermo(@Param("termo") String termo);

    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :minPreco AND :maxPreco " +
            "AND p.ativo = true")
    List<Produto> findByPrecoBetween(@Param("minPreco") BigDecimal minPreco,
                                     @Param("maxPreco") BigDecimal maxPreco);

    @Query("SELECT p FROM Produto p WHERE p.loja.id = :lojaId AND p.ativo = true " +
            "ORDER BY p.totalVendas DESC")
    Page<Produto> findMaisVendidosPorLoja(@Param("lojaId") Long lojaId, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.destaque = true AND p.ativo = true " +
            "ORDER BY p.dataCriacao DESC")
    Page<Produto> findProdutosDestaque(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.loja.id = :lojaId AND p.ativo = true")
    Long countProdutosAtivosPorLoja(@Param("lojaId") Long lojaId);
}