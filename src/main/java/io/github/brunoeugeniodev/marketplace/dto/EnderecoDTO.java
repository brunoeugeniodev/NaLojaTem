package io.github.brunoeugeniodev.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoDTO {
    private Long id;
    private Long usuarioId;

    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String complemento;
    private Boolean enderecoPrincipal;

    // Removendo campos redundantes - os métodos já calculam
    // private String enderecoCompleto;
    // private String enderecoCompletoComCEP;

    // Métodos para formatar endereço
    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s, %s/%s",
                rua != null ? rua : "",
                numero != null ? numero : "",
                bairro != null ? bairro : "",
                cidade != null ? cidade : "",
                estado != null ? estado : "");
    }

    public String getEnderecoCompletoComCEP() {
        if (cep != null && !cep.isEmpty()) {
            return String.format("%s, %s - %s, %s/%s - CEP: %s",
                    rua != null ? rua : "",
                    numero != null ? numero : "",
                    bairro != null ? bairro : "",
                    cidade != null ? cidade : "",
                    estado != null ? estado : "",
                    cep);
        }
        return getEnderecoCompleto();
    }
}