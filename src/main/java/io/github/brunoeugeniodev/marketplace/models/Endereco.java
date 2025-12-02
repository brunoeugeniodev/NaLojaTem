package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "enderecos",
        indexes = {
                @Index(name = "idx_endereco_usuario", columnList = "usuario_id"),
                @Index(name = "idx_endereco_cidade", columnList = "cidade")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_endereco_usuario"))
    @JsonIgnore // Evita loop infinito
    private Usuario usuario;

    // REMOVA ESTE RELACIONAMENTO - Ele não existe na Loja
    // @OneToOne(mappedBy = "endereco", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // @JsonIgnore
    // private Loja loja;

    @NotBlank(message = "Rua é obrigatória")
    @Size(max = 200, message = "Rua deve ter no máximo 200 caracteres")
    @Column(name = "rua", nullable = false, length = 200)
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    @Column(name = "bairro", nullable = false, length = 100)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    @Column(name = "cidade", nullable = false, length = 100)
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (UF)")
    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    @Size(max = 20, message = "CEP deve ter no máximo 20 caracteres")
    @Column(name = "cep", length = 20)
    private String cep;

    @Size(max = 200, message = "Complemento deve ter no máximo 200 caracteres")
    @Column(name = "complemento", length = 200)
    private String complemento;

    @Column(name = "endereco_principal", nullable = false)
    private Boolean enderecoPrincipal = false;

    // Método helper para criar endereço completo
    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s, %s/%s",
                rua, numero, bairro, cidade, estado);
    }

    // Método helper para criar endereço com CEP
    public String getEnderecoCompletoComCEP() {
        if (cep != null && !cep.isEmpty()) {
            return String.format("%s, %s - %s, %s/%s - CEP: %s",
                    rua, numero, bairro, cidade, estado, cep);
        }
        return getEnderecoCompleto();
    }
}