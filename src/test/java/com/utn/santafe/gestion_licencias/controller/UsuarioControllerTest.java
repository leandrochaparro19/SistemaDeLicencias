package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.auditoria.AuditoriaUsuario;
import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.service.AuditoriaService;
import com.utn.santafe.gestion_licencias.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private AuditoriaService auditoriaService;

    private Usuario usuario;
    private AuditoriaUsuario auditoria;

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
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        // Crear auditoría de prueba
        auditoria = new AuditoriaUsuario();
        auditoria.setId(1L);
        auditoria.setUsuarioModificadoId(1L);
        auditoria.setUsuarioModificadoDni("12345678");
        auditoria.setUsuarioQueModificaId(2L);
        auditoria.setUsuarioQueModificaDni("87654321");
        auditoria.setCampoModificado("nombre");
        auditoria.setValorAnterior("Juan");
        auditoria.setValorNuevo("Juan Carlos");
        auditoria.setTipoOperacion(TipoOperacion.MODIFICACION);
        auditoria.setFechaModificacion(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void listarUsuarios_DebeRetornarListado() throws Exception {
        // Given
        when(usuarioService.listarTodos()).thenReturn(Arrays.asList(usuario));

        // When/Then
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(model().attributeExists("usuarios"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void mostrarFormularioNuevo_DebeRetornarFormulario() throws Exception {
        // When/Then
        mockMvc.perform(get("/usuarios/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/crear-usuario"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("sexos"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void guardarUsuario_DebeGuardarUsuarioYRedirigir() throws Exception {
        // Given
        when(usuarioService.existeDni("12345678")).thenReturn(false);
        when(usuarioService.existeEmail("juan.perez@example.com")).thenReturn(false);
        when(usuarioService.guardarUsuario(any(Usuario.class))).thenReturn(usuario);

        // When/Then
        mockMvc.perform(post("/usuarios/guardar")
                        .param("nombre", "Juan")
                        .param("apellido", "Pérez")
                        .param("dni", "12345678")
                        .param("fechaNacimiento", "1980-01-01")
                        .param("sexo", "MASCULINO")
                        .param("direccion", "Calle Falsa 123")
                        .param("email", "juan.perez@example.com")
                        .param("telefono", "1122334455")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void verUsuario_DebeRetornarDetalleUsuario() throws Exception {
        // Given
        when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(auditoriaService.obtenerAuditoriaUsuario(1L)).thenReturn(Arrays.asList(auditoria));

        // When/Then
        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/detalle"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("historialAuditoria"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void mostrarFormularioEditar_DebeRetornarFormulario() throws Exception {
        // Given
        when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        // When/Then
        mockMvc.perform(get("/usuarios/1/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/editar-usuario"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void mostrarFormularioCambiarPassword_DebeRetornarFormulario() throws Exception {
        // Given
        when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        // When/Then
        mockMvc.perform(get("/usuarios/1/cambiar-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/cambiar-password"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SUPERUSER"})
    void verAuditoriaUsuario_DebeRetornarAuditoria() throws Exception {
        // Given
        when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(auditoriaService.obtenerAuditoriaUsuario(1L)).thenReturn(Arrays.asList(auditoria));

        // When/Then
        mockMvc.perform(get("/usuarios/1/auditoria"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/auditoria"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("historialAuditoria"));
    }
}
