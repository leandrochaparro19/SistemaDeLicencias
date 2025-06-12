package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;

    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba
        usuarioActivo = new Usuario();
        usuarioActivo.setNombre("Juan");
        usuarioActivo.setApellido("Pérez");
        usuarioActivo.setDni("12345678");
        usuarioActivo.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        usuarioActivo.setSexo(Sexo.MASCULINO);
        usuarioActivo.setDireccion("Calle Falsa 123");
        usuarioActivo.setEmail("juan.perez@example.com");
        usuarioActivo.setTelefono("1122334455");
        usuarioActivo.setPassword("password123");
        usuarioActivo.setRol(Rol.ADMIN);
        usuarioActivo.setActivo(true);
        usuarioActivo.setFechaCreacion(LocalDateTime.now());
        entityManager.persist(usuarioActivo);

        usuarioInactivo = new Usuario();
        usuarioInactivo.setNombre("María");
        usuarioInactivo.setApellido("González");
        usuarioInactivo.setDni("87654321");
        usuarioInactivo.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        usuarioInactivo.setSexo(Sexo.FEMENINO);
        usuarioInactivo.setDireccion("Avenida Siempreviva 742");
        usuarioInactivo.setEmail("maria.gonzalez@example.com");
        usuarioInactivo.setTelefono("5544332211");
        usuarioInactivo.setPassword("password456");
        usuarioInactivo.setRol(Rol.ADMIN);
        usuarioInactivo.setActivo(false);
        usuarioInactivo.setFechaCreacion(LocalDateTime.now());
        entityManager.persist(usuarioInactivo);

        entityManager.flush();
    }

    @Test
    void findByDni_DebeRetornarUsuario() {
        // When
        Optional<Usuario> found = usuarioRepository.findByDni("12345678");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("Juan");
    }

    @Test
    void findByEmail_DebeRetornarUsuario() {
        // When
        Optional<Usuario> found = usuarioRepository.findByEmail("juan.perez@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDni()).isEqualTo("12345678");
    }

    @Test
    void findByActivoTrue_DebeRetornarUsuariosActivos() {
        // When
        List<Usuario> usuarios = usuarioRepository.findByActivoTrue();

        // Then
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getDni()).isEqualTo("12345678");
    }

    @Test
    void findByActivoFalse_DebeRetornarUsuariosInactivos() {
        // When
        List<Usuario> usuarios = usuarioRepository.findByActivoFalse();

        // Then
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getDni()).isEqualTo("87654321");
    }

    @Test
    void existsByDni_DebeRetornarTrue() {
        // When
        boolean exists = usuarioRepository.existsByDni("12345678");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_DebeRetornarTrue() {
        // When
        boolean exists = usuarioRepository.existsByEmail("juan.perez@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByDniAndIdNot_DebeRetornarFalse() {
        // When
        boolean exists = usuarioRepository.existsByDniAndIdNot("12345678", usuarioActivo.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmailAndIdNot_DebeRetornarFalse() {
        // When
        boolean exists = usuarioRepository.existsByEmailAndIdNot("juan.perez@example.com", usuarioActivo.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void countByActivoTrue_DebeRetornarCantidadCorrecta() {
        // When
        long count = usuarioRepository.countByActivoTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByActivoFalse_DebeRetornarCantidadCorrecta() {
        // When
        long count = usuarioRepository.countByActivoFalse();

        // Then
        assertThat(count).isEqualTo(1);
    }
}
