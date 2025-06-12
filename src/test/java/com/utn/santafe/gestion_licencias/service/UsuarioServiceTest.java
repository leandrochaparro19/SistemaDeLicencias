package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuarioModificador;

    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba
        usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setNombre("Juan");
        usuario1.setApellido("Pérez");
        usuario1.setDni("12345678");
        usuario1.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        usuario1.setSexo(Sexo.MASCULINO);
        usuario1.setDireccion("Calle Falsa 123");
        usuario1.setEmail("juan.perez@example.com");
        usuario1.setTelefono("1122334455");
        usuario1.setPassword("password123");
        usuario1.setRol(Rol.ADMIN);
        usuario1.setActivo(true);
        usuario1.setFechaCreacion(LocalDateTime.now());

        usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNombre("María");
        usuario2.setApellido("González");
        usuario2.setDni("87654321");
        usuario2.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        usuario2.setSexo(Sexo.FEMENINO);
        usuario2.setDireccion("Avenida Siempreviva 742");
        usuario2.setEmail("maria.gonzalez@example.com");
        usuario2.setTelefono("5544332211");
        usuario2.setPassword("password456");
        usuario2.setRol(Rol.ADMIN);
        usuario2.setActivo(false);
        usuario2.setFechaCreacion(LocalDateTime.now());

        usuarioModificador = new Usuario();
        usuarioModificador.setId(3L);
        usuarioModificador.setNombre("Admin");
        usuarioModificador.setApellido("Sistema");
        usuarioModificador.setDni("11223344");
        usuarioModificador.setRol(Rol.SUPERUSER);
    }

    @Test
    void guardarUsuario_DebeGuardarUsuario() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // When
        Usuario result = usuarioService.guardarUsuario(usuario1);

        // Then
        assertThat(result).isEqualTo(usuario1);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(usuario1);
    }

    @Test
    void actualizarUsuario_DebeActualizarUsuario() {
        // Given
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Juan Carlos");
        usuarioActualizado.setApellido("Pérez");
        usuarioActualizado.setEmail("juancarlos.perez@example.com");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // When
        Usuario result = usuarioService.actualizarUsuario(1L, usuarioActualizado, usuarioModificador);

        // Then
        assertThat(result.getNombre()).isEqualTo("Juan Carlos");
        assertThat(result.getEmail()).isEqualTo("juancarlos.perez@example.com");
        verify(usuarioRepository).save(usuario1);
        verify(auditoriaService).auditarCambioUsuario(any(), any(), eq(usuarioModificador), eq(TipoOperacion.MODIFICACION));
    }

    @Test
    void actualizarUsuario_DebeArrojarExcepcionSiUsuarioNoExiste() {
        // Given
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> usuarioService.actualizarUsuario(999L, new Usuario(), usuarioModificador))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    void cambiarPassword_DebeCambiarPassword() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(passwordEncoder.encode("nuevaPassword")).thenReturn("encodedNuevaPassword");

        // When
        usuarioService.cambiarPassword(1L, "nuevaPassword", usuarioModificador);

        // Then
        assertThat(usuario1.getPassword()).isEqualTo("encodedNuevaPassword");
        verify(usuarioRepository).save(usuario1);
        verify(auditoriaService).auditarOperacion(eq(usuario1), eq(usuarioModificador), eq(TipoOperacion.CAMBIO_PASSWORD), anyString());
    }

    @Test
    void toggleEstadoUsuario_DebeCambiarEstadoUsuario() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario1);

        // When
        usuarioService.toggleEstadoUsuario(1L, usuarioModificador);

        // Then
        assertThat(usuario1.isActivo()).isFalse();
        verify(usuarioRepository).save(usuario1);
        verify(auditoriaService).auditarCambioUsuario(any(), any(), eq(usuarioModificador), eq(TipoOperacion.DESACTIVACION));
    }

    @Test
    void buscarPorDni_DebeRetornarUsuario() {
        // Given
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuario1));

        // When
        Optional<Usuario> result = usuarioService.buscarPorDni("12345678");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(usuario1);
        verify(usuarioRepository).findByDni("12345678");
    }

    @Test
    void listarTodos_DebeRetornarTodosLosUsuarios() {
        // Given
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario1, usuario2));

        // When
        List<Usuario> result = usuarioService.listarTodos();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(usuario1, usuario2);
        verify(usuarioRepository).findAll();
    }

    @Test
    void listarActivos_DebeRetornarUsuariosActivos() {
        // Given
        when(usuarioRepository.findByActivoTrue()).thenReturn(Arrays.asList(usuario1));

        // When
        List<Usuario> result = usuarioService.listarActivos();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(usuario1);
        verify(usuarioRepository).findByActivoTrue();
    }

    @Test
    void existeDni_DebeRetornarTrue() {
        // Given
        when(usuarioRepository.existsByDni("12345678")).thenReturn(true);

        // When
        boolean result = usuarioService.existeDni("12345678");

        // Then
        assertThat(result).isTrue();
        verify(usuarioRepository).existsByDni("12345678");
    }

    @Test
    void eliminarUsuario_DebeEliminarUsuario() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));

        // When
        usuarioService.eliminarUsuario(1L, usuarioModificador);

        // Then
        verify(auditoriaService).auditarOperacion(eq(usuario1), eq(usuarioModificador), eq(TipoOperacion.ELIMINACION), anyString());
        verify(usuarioRepository).deleteById(1L);
    }
}
