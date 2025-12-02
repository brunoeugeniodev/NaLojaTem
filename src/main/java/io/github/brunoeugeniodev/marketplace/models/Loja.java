package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lojas",
        indexes = {
                @Index(name = "idx_loja_cnpj", columnList = "cnpj", unique = true),
                @Index(name = "idx_loja_nome", columnList = "nome"),
                @Index(name = "idx_loja_usuario", columnList = "usuario_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_loja_usuario"))
    @JsonIgnore // Evita loop infinito
    private Usuario usuario;

    // LOJA TEM SEU PRÓPRIO ENDEREÇO (não compartilhado com usuário)
    // Isso evita o problema do relacionamento conflitante
    @Embedded
    private EnderecoLoja endereco;

    @OneToMany(mappedBy = "loja", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // Evita loop infinito
    private List<Produto> produtos = new ArrayList<>();

    @NotBlank(message = "Nome da loja é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos")
    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "site", length = 200)
    private String site;

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

    @Column(name = "total_avaliacoes")
    private Integer totalAvaliacoes = 0;

    // Método helper para obter informações básicas da loja
    @Transient
    @JsonInclude
    public String getLocalizacao() {
        if (endereco != null) {
            return String.format("%s, %s", endereco.getCidade(), endereco.getEstado());
        }
        return "Localização não informada";
    }

    // Método helper para contar produtos
    @Transient
    @JsonInclude
    public Integer getQuantidadeProdutos() {
        return produtos != null ? produtos.size() : 0;
    }

    // Classe interna para endereço da loja
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class EnderecoLoja {

        @NotBlank(message = "Rua é obrigatória")
        @Size(max = 200, message = "Rua deve ter no máximo 200 caracteres")
        @Column(name = "loja_rua", length = 200)
        private String rua;

        @NotBlank(message = "Número é obrigatório")
        @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
        @Column(name = "loja_numero", length = 20)
        private String numero;

        @NotBlank(message = "Bairro é obrigatório")
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        @Column(name = "loja_bairro", length = 100)
        private String bairro;

        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        @Column(name = "loja_cidade", length = 100)
        private String cidade;

        @NotBlank(message = "Estado é obrigatório")
        @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (UF)")
        @Column(name = "loja_estado", length = 2)
        private String estado;

        @Size(max = 20, message = "CEP deve ter no máximo 20 caracteres")
        @Column(name = "loja_cep", length = 20)
        private String cep;

        @Size(max = 200, message = "Complemento deve ter no máximo 200 caracteres")
        @Column(name = "loja_complemento", length = 200)
        private String complemento;

        public String getEnderecoCompleto() {
            return String.format("%s, %s - %s, %s/%s",
                    rua, numero, bairro, cidade, estado);
        }
    }
}