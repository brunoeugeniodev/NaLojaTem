package io.github.brunoeugeniodev.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LojaDTO {
    private Long id;
    private String nome;
    private String cnpj;
    private String descricao;
    private String fotoUrl;
    private String telefone;
    private String email;
    private String site;
    private Boolean ativo;

    private Double avaliacaoMedia;
    private Integer totalAvaliacoes;
    private Integer quantidadeProdutos;
    private String localizacao;

    // Informações do proprietário (apenas para admin)
    private Long proprietarioId;
    private String proprietarioNome;

    // Endereço da loja
    private EnderecoLojaDTO endereco;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataCriacao;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataAtualizacao;

    // Método para formatar CNPJ
    public String getCnpjFormatado() {
        if (cnpj != null && cnpj.length() == 14) {
            return cnpj.substring(0, 2) + "." +
                    cnpj.substring(2, 5) + "." +
                    cnpj.substring(5, 8) + "/" +
                    cnpj.substring(8, 12) + "-" +
                    cnpj.substring(12);
        }
        return cnpj;
    }
}