package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.ItemCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {

    List<ItemCarrinho> findByCarrinhoId(Long carrinhoId);

    Optional<ItemCarrinho> findByCarrinhoIdAndProdutoId(Long carrinhoId, Long produtoId);

    @Query("SELECT i FROM ItemCarrinho i JOIN FETCH i.produto WHERE i.carrinho.id = :carrinhoId")
    List<ItemCarrinho> findByCarrinhoIdComProduto(@Param("carrinhoId") Long carrinhoId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ItemCarrinho i WHERE i.carrinho.id = :carrinhoId")
    void deleteAllByCarrinhoId(@Param("carrinhoId") Long carrinhoId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ItemCarrinho i WHERE i.carrinho.id = :carrinhoId AND i.id = :itemId")
    void deleteByCarrinhoIdAndId(@Param("carrinhoId") Long carrinhoId, @Param("itemId") Long itemId);

    @Query("SELECT SUM(i.quantidade) FROM ItemCarrinho i WHERE i.carrinho.id = :carrinhoId")
    Integer countTotalItensNoCarrinho(@Param("carrinhoId") Long carrinhoId);
}