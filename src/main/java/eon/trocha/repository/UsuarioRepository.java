package eon.trocha.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import eon.trocha.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}