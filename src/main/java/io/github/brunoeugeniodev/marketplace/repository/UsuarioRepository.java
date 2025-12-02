package io.github.brunoeugeniodev.marketplace.repository;

import io.github.brunoeugeniodev.marketplace.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<Usuario> findByAtivoTrue();

    List<Usuario> findByRolesContains(String role);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Usuario> buscarPorNome(@Param("nome") String nome);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.cpf = :cpf AND u.id != :id")
    boolean existsByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") Long id);

    // Método para contar usuários ativos
    long countByAtivoTrue();

    // Opcional: método para contar usuários por role
    @Query("SELECT COUNT(u) FROM Usuario u WHERE :role MEMBER OF u.roles AND u.ativo = true")
    long countByRoleAndAtivoTrue(@Param("role") String role);

    // Opcional: buscar usuários por nome e ativo
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND u.ativo = :ativo")
    List<Usuario> buscarPorNomeEStatus(@Param("nome") String nome, @Param("ativo") boolean ativo);

    // Opcional: buscar por email ignorando case
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Opcional: verificar se existe email ignorando case
    boolean existsByEmailIgnoreCase(String email);
}