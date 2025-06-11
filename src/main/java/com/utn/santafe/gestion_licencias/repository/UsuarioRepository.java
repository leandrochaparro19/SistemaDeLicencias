package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByDni(String dni);

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByActivoFalse();

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByActivoTrue();

    long countByActivoFalse();
}
