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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

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
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "12345678", roles = {"ADMIN"})
    void mostrarDashboard_DebeRetornarVistaDashboard() throws Exception {
        // Given
        when(usuarioService.buscarPorDni("12345678")).thenReturn(Optional.of(usuario));
        when(usuarioService.contarUsuarios()).thenReturn(10L);
        when(usuarioService.contarUsuariosActivos()).thenReturn(8L);
        when(usuarioService.contarUsuariosInactivos()).thenReturn(2L);

        // When/Then
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("totalUsuarios"))
                .andExpect(model().attributeExists("usuariosActivos"))
                .andExpect(model().attributeExists("usuariosInactivos"));
    }
}
