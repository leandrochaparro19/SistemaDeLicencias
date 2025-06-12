package com.utn.santafe.gestion_licencias.service;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuarioActivo;
    private Usuario usuarioInactivo;

    @BeforeEach
    void setUp() {
        // Crear usuario activo
        usuarioActivo = new Usuario();
        usuarioActivo.setId(1L);
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

        // Crear usuario inactivo
        usuarioInactivo = new Usuario();
        usuarioInactivo.setId(2L);
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
    }

    @Test
    void loadUserByUsername_DebeRetornarUserDetailsParaUsuarioActivo() {
        // Given
        when(usuarioRepository.findByDni("12345678")).thenReturn(Optional.of(usuarioActivo));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("12345678");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("12345678");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        verify(usuarioRepository).findByDni("12345678");
    }

    @Test
    void loadUserByUsername_DebeArrojarExcepcionParaUsuarioInexistente() {
        // Given
        when(usuarioRepository.findByDni("99999999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("99999999"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado con DNI: 99999999");
        verify(usuarioRepository).findByDni("99999999");
    }

    @Test
    void loadUserByUsername_DebeArrojarExcepcionParaUsuarioInactivo() {
        // Given
        when(usuarioRepository.findByDni("87654321")).thenReturn(Optional.of(usuarioInactivo));

        // When/Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("87654321"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario inactivo: 87654321");
        verify(usuarioRepository).findByDni("87654321");
    }
}
