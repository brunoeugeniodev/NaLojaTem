package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Loja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LojaRepository extends JpaRepository<Loja, Long> {

    boolean existsByCnpj(String cnpj);

    List<Loja> findByNomeContainingIgnoreCase(String nome);

    // Método para buscar lojas por usuário (quando tiver o relacionamento)
    // List<Loja> findByUsuarioEmail(String email);

    Optional<Loja> findByCnpj(String cnpj);
}