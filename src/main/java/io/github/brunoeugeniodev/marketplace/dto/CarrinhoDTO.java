package io.github.brunoeugeniodev.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarrinhoDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioEmail;

    private List<ItemCarrinhoDTO> itens;

    // Campos removidos para evitar conflito com os métodos getters
    // private BigDecimal total;
    // private Integer totalItens;
    // private Boolean vazio;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Método para calcular total
    public BigDecimal getTotal() {
        if (itens != null && !itens.isEmpty()) {
            return itens.stream()
                    .map(item -> item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return BigDecimal.ZERO;
    }

    public Integer getTotalItens() {
        if (itens != null) {
            return itens.stream()
                    .mapToInt(item -> item.getQuantidade() != null ? item.getQuantidade() : 0)
                    .sum();
        }
        return 0;
    }

    public Boolean getVazio() {
        return itens == null || itens.isEmpty();
    }
}