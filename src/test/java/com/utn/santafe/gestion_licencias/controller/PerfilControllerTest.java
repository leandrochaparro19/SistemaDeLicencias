package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PerfilController.class)
public class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setDni("12345678");
        usuario.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        usuario.setSexo(Sexo.MASCULINO);
        usuario.setDireccion("Calle Falsa 123");
        usuario.setEmail("juan.perez@example.com");
        usuario.setTelefono("1122334455");
        usuario.setPassword("password123");
        usuario.setRol(Rol.ADMIN_CON_PERMISOS);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN_CON_PERMISOS"})
    void verPerfil_DebeRetornarVistaPerfil() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));

        // When/Then
        mockMvc.perform(get("/perfil"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil/mi-perfil"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN_CON_PERMISOS"})
    void mostrarFormularioEditar_DebeRetornarFormulario() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));

        // When/Then
        mockMvc.perform(get("/perfil/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil/editar-perfil"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("sexos"));
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN_CON_PERMISOS"})
    void actualizarPerfil_DebeActualizarPerfilYRedirigir() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));
        when(usuarioService.existeEmail("juan.perez@example.com")).thenReturn(false);
        when(usuarioService.actualizarUsuario(anyLong(), any(Usuario.class), any(Usuario.class))).thenReturn(usuario);

        // When/Then
        mockMvc.perform(post("/perfil/actualizar")
                        .param("nombre", "Juan Carlos")
                        .param("apellido", "Pérez")
                        .param("direccion", "Calle Nueva 456")
                        .param("email", "juan.perez@example.com")
                        .param("telefono", "1122334455")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN_CON_PERMISOS"})
    void mostrarFormularioCambiarPassword_DebeRetornarFormulario() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));

        // When/Then
        mockMvc.perform(get("/perfil/cambiar-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("perfil/cambiar-mi-password"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN_CON_PERMISOS"})
    void cambiarPassword_DebeCambiarPasswordYRedirigir() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("passwordActual", "password123")).thenReturn(true);

        // When/Then
        mockMvc.perform(post("/perfil/cambiar-password")
                        .param("passwordActual", "passwordActual")
                        .param("nuevaPassword", "nuevaPassword123")
                        .param("confirmarPassword", "nuevaPassword123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));
    }
}
