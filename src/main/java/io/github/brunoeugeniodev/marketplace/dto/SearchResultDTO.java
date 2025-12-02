package io.github.brunoeugeniodev.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {
    private List<LojaDTO> lojas;
    private List<ProdutoDTO> produtos;

    // Campos removidos para evitar conflito com os métodos getters
    // private Integer totalLojas;
    // private Integer totalProdutos;

    public Integer getTotalLojas() {
        return lojas != null ? lojas.size() : 0;
    }

    public Integer getTotalProdutos() {
        return produtos != null ? produtos.size() : 0;
    }
}