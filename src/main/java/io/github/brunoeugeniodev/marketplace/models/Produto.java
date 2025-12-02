package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Table(name = "produtos",
        indexes = {
                @Index(name = "idx_produto_nome", columnList = "nome"),
                @Index(name = "idx_produto_loja", columnList = "loja_id"),
                @Index(name = "idx_produto_preco", columnList = "preco"),
                @Index(name = "idx_produto_destaque", columnList = "destaque")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_produto_loja"))
    @JsonIgnore // Evita loop infinito
    private Loja loja;

    @NotBlank(message = "Nome do produto é obrigatório")
    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres")
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Column(name = "descricao", length = 1000)
    private String descricao;

    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Min(value = 0, message = "Quantidade não pode ser negativa")
    @Column(name = "quantidade", nullable = false)
    private Long quantidade;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "destaque", nullable = false)
    private Boolean destaque = false;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "avaliacao_media")
    private Double avaliacaoMedia = 0.0;

    @Column(name = "total_vendas")
    private Integer totalVendas = 0;

    // Método helper para verificar disponibilidade
    @Transient
    public boolean isDisponivel() {
        return ativo && quantidade > 0;
    }

    // Método helper para obter preço formatado
    @Transient
    public String getPrecoFormatado() {
        return String.format("R$ %.2f", preco);
    }
}