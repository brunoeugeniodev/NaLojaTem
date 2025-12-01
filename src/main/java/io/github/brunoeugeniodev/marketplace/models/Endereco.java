package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @JsonBackReference
    private Usuario usuario;

    // Alterado para OneToOne bidirecional
    @OneToOne(mappedBy = "endereco", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Loja loja;

    @Column
    private String rua;

    @Column
    private String bairro;

    @Column
    private String cidade;

    @Column
    private String numero;

    @Column
    private String estado;

    // Método helper para criar endereço completo
    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s, %s - %s",
                rua, numero, bairro, cidade, estado);
    }
}