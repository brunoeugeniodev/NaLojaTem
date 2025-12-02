package io.github.brunoeugeniodev.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdutoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    // Campo removido para evitar conflito com o método getPrecoFormatado()
    // private String precoFormatado;

    private Long quantidade;
    private String fotoUrl;
    private String categoria;
    private String marca;
    private String modelo;
    private Boolean destaque;
    private Boolean ativo;
    private Boolean disponivel;

    // Informações da loja
    private Long lojaId;
    private String lojaNome;
    private String lojaCnpj;

    // Estatísticas
    private Double avaliacaoMedia;
    private Integer totalVendas;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Método para obter preço formatado
    public String getPrecoFormatado() {
        if (preco != null) {
            return String.format("R$ %.2f", preco);
        }
        return "R$ 0,00";
    }
}