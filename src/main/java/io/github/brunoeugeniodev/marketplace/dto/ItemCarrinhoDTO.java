package io.github.brunoeugeniodev.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarrinhoDTO {
    private Long id;
    private Long carrinhoId;

    // Informações do produto
    private Long produtoId;
    private String produtoNome;
    private String produtoDescricao;
    private BigDecimal produtoPreco;
    private String produtoFotoUrl;
    private Long produtoQuantidadeDisponivel;
    private Boolean produtoDisponivel;

    // Informações do item
    private Integer quantidade;
    private BigDecimal precoUnitario;

    // Campo removido para evitar conflito com o método getSubtotal()
    // private BigDecimal subtotal;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataAdicao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Método para calcular subtotal
    public BigDecimal getSubtotal() {
        if (precoUnitario != null && quantidade != null) {
            return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
        return BigDecimal.ZERO;
    }
}