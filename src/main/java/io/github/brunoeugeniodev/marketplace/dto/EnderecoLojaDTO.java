package io.github.brunoeugeniodev.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoLojaDTO {
    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String complemento;

    // Removendo a redundância - o método getEnderecoCompleto() já calcula
    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s, %s/%s",
                rua != null ? rua : "",
                numero != null ? numero : "",
                bairro != null ? bairro : "",
                cidade != null ? cidade : "",
                estado != null ? estado : "");
    }
}