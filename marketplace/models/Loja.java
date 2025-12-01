package io.github.brunoeugeniodev.marketplace.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "lojas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    // Alterado para ManyToOne (uma loja tem um endereço, mas um endereço pode ter uma loja)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco")
    @JsonManagedReference
    private Endereco endereco;

    @OneToMany(mappedBy = "loja", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Produto> produtos;

    @Column
    private String nome;

    @Column
    private String cnpj;

    @Column
    private String foto;

    @Column(length = 500)
    private String descricao;
}