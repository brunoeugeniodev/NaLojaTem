package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "itens_carrinho",
        indexes = {
                @Index(name = "idx_item_carrinho", columnList = "carrinho_id"),
                @Index(name = "idx_item_produto", columnList = "produto_id"),
                @Index(name = "idx_item_carrinho_produto", columnList = "carrinho_id,produto_id", unique = true)
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_item_carrinho_produto",
                        columnNames = {"carrinho_id", "produto_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrinho_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_carrinho"))
    @JsonIgnore // Evita loop infinito
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_produto"))
    private Produto produto;

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @CreationTimestamp
    @Column(name = "data_adicao", updatable = false)
    private LocalDateTime dataAdicao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Método pré-persist para capturar preço atual
    @PrePersist
    @PreUpdate
    public void capturarPreco() {
        if (produto != null && precoUnitario == null) {
            this.precoUnitario = produto.getPreco();
        }
    }

    // Método para calcular subtotal
    @Transient
    public BigDecimal getSubtotal() {
        if (precoUnitario == null || quantidade == null) {
            return BigDecimal.ZERO;
        }
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    // Método para atualizar quantidade
    public void atualizarQuantidade(Integer novaQuantidade) {
        if (novaQuantidade != null && novaQuantidade > 0) {
            this.quantidade = novaQuantidade;
        }
    }
}