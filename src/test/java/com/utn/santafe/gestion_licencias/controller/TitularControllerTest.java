package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.*;
import com.utn.santafe.gestion_licencias.service.LicenciaService;
import com.utn.santafe.gestion_licencias.service.TitularService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TitularController.class)
public class TitularControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TitularService titularService;

    @MockBean
    private LicenciaService licenciaService;

    private Titular titular;
    private Licencia licencia;

    @BeforeEach
    void setUp() {
        // Crear titular de prueba
        titular = new Titular();
        titular.setId(1L);
        titular.setTipoDocumento(TipoDocumento.DNI);
        titular.setNumeroDocumento("12345678");
        titular.setApellido("Pérez");
        titular.setNombre("Juan");
        titular.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        titular.setDireccion("Calle Falsa 123");
        titular.setClaseSolicitada(ClaseLicencia.B);
        titular.setGrupoSanguineo(GrupoSanguineo.A);
        titular.setFactorRh(FactorRh.POS);
        titular.setDonanteOrganos(true);

        // Crear licencia de prueba
        licencia = new Licencia();
        licencia.setId(1L);
        licencia.setTitular(titular);
        licencia.setClase(ClaseLicencia.B);
        licencia.setFechaEmision(LocalDate.now().minusMonths(1));
        licencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        licencia.setUsuarioAdmin("admin");
        licencia.setObservaciones("Licencia de prueba");
        licencia.setVigente(true);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void mostrarFormularioAlta_DebeRetornarFormulario() throws Exception {
        // When/Then
        mockMvc.perform(get("/titulares/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeExists("titular"))
                .andExpect(model().attributeExists("tiposDoc"))
                .andExpect(model().attributeExists("clasesLicencia"))
                .andExpect(model().attributeExists("grupos"))
                .andExpect(model().attributeExists("factores"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void guardar_DebeGuardarTitularYRedirigir() throws Exception {
        // Given
        when(titularService.guardarTitular(any(Titular.class))).thenReturn(titular);

        // When/Then
        mockMvc.perform(post("/titulares/guardar")
                        .param("tipoDocumento", "DNI")
                        .param("numeroDocumento", "12345678")
                        .param("apellido", "Pérez")
                        .param("nombre", "Juan")
                        .param("fechaNacimiento", "1980-01-01")
                        .param("direccion", "Calle Falsa 123")
                        .param("claseSolicitada", "B")
                        .param("grupoSanguineo", "A")
                        .param("factorRh", "POS")
                        .param("donanteOrganos", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listarTitulares_DebeRetornarListado() throws Exception {
        // Given
        when(titularService.listarTitulares()).thenReturn(Arrays.asList(titular));

        // When/Then
        mockMvc.perform(get("/titulares/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/lista"))
                .andExpect(model().attributeExists("titulares"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void editar_DebeRetornarFormularioEdicion() throws Exception {
        // Given
        when(titularService.buscarPorId(1L)).thenReturn(titular);

        // When/Then
        mockMvc.perform(get("/titulares/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeExists("titular"))
                .andExpect(model().attributeExists("tiposDoc"))
                .andExpect(model().attributeExists("clasesLicencia"))
                .andExpect(model().attributeExists("grupos"))
                .andExpect(model().attributeExists("factores"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void confirmarEliminar_DebeRetornarConfirmacion() throws Exception {
        // Given
        when(titularService.buscarPorId(1L)).thenReturn(titular);

        // When/Then
        mockMvc.perform(get("/titulares/eliminar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/confirmar-eliminar"))
                .andExpect(model().attributeExists("titular"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void eliminar_DebeEliminarTitularYRedirigir() throws Exception {
        // When/Then
        mockMvc.perform(post("/titulares/eliminar/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listarHistorialPorTitular_DebeRetornarHistorial() throws Exception {
        // Given
        when(licenciaService.listarHistorialPorTitular(1L)).thenReturn(Arrays.asList(licencia));

        // When/Then
        mockMvc.perform(get("/titulares/titular/1/historial"))
                .andExpect(status().isOk())
                .andExpect(view().name("Titulares/historialTitular"))
                .andExpect(model().attributeExists("historialLicencias"));
    }
}
