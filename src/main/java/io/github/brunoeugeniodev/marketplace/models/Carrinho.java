package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carrinhos",
        indexes = {
                @Index(name = "idx_carrinho_usuario", columnList = "usuario_id", unique = true)
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false,
            foreignKey = @ForeignKey(name = "fk_carrinho_usuario"))
    @JsonIgnore // Evita loop infinito
    private Usuario usuario;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrinho> itens = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    // Método helper para calcular total
    @Transient
    public BigDecimal getTotal() {
        if (itens == null || itens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método helper para contar itens
    @Transient
    public int getTotalItens() {
        if (itens == null) {
            return 0;
        }
        return itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
    }

    // Método helper para verificar se está vazio
    @Transient
    public boolean isEmpty() {
        return itens == null || itens.isEmpty();
    }

    // Método para adicionar item
    public void adicionarItem(ItemCarrinho item) {
        if (itens == null) {
            itens = new ArrayList<>();
        }
        item.setCarrinho(this);
        itens.add(item);
    }

    // Método para remover item
    public void removerItem(ItemCarrinho item) {
        if (itens != null) {
            itens.remove(item);
            item.setCarrinho(null);
        }
    }

    // Método para limpar carrinho
    public void limpar() {
        if (itens != null) {
            itens.clear();
        }
    }
}