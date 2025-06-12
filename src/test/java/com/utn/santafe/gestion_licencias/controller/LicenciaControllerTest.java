package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import com.utn.santafe.gestion_licencias.service.LicenciaService;
import com.utn.santafe.gestion_licencias.service.TitularService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LicenciaController.class)
public class LicenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TitularRepository titularRepository;

    @MockBean
    private TitularService titularService;

    @MockBean
    private LicenciaService licenciaService;

    @MockBean
    private LicenciaRepository licenciaRepository;

    private Titular titular;
    private Licencia licencia;

    @BeforeEach
    void setUp() {
        // Crear titular de prueba
        titular = new Titular();
        titular.setId(1L);
        titular.setTipoDocumento(com.utn.santafe.gestion_licencias.model.titular.TipoDocumento.DNI);
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
        licencia.setTipoEmision(tipoEmision.ORIGINAL);
        licencia.setCosto(new BigDecimal("48.00"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void nuevaLicencia_DebeRetornarFormulario() throws Exception {
        // Given
        when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

        // When/Then
        mockMvc.perform(get("/licencias/nueva/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/form"))
                .andExpect(model().attributeExists("titular"))
                .andExpect(model().attributeExists("clases"))
                .andExpect(model().attributeExists("licenciaForm"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void guardarLicencia_DebeGuardarLicenciaYRedirigir() throws Exception {
        // Given
        when(licenciaService.emitirLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenReturn(licencia);

        // When/Then
        mockMvc.perform(post("/licencias/guardar")
                        .param("titularId", "1")
                        .param("clase", "B")
                        .param("observaciones", "Observaciones de prueba")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void verListadoLicencias_DebeRetornarListado() throws Exception {
        // Given
        when(licenciaService.listarLicencias()).thenReturn(Arrays.asList(licencia));

        // When/Then
        mockMvc.perform(get("/licencias/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/lista"))
                .andExpect(model().attributeExists("licencias"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void imprimirLicencia_DebeRetornarVistaImpresion() throws Exception {
        // Given
        when(licenciaService.buscarPorId(1L)).thenReturn(licencia);

        // When/Then
        mockMvc.perform(get("/licencias/1/imprimirLicencia"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/imprimirLicencia"))
                .andExpect(model().attributeExists("licencia"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void mostrarFormularioRenovar_DebeRetornarFormulario() throws Exception {
        // Given
        when(licenciaService.buscarPorId(1L)).thenReturn(licencia);

        // When/Then
        mockMvc.perform(get("/licencias/1/renovar"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formRenovar"))
                .andExpect(model().attributeExists("licenciaActual"))
                .andExpect(model().attributeExists("licenciaForm"))
                .andExpect(model().attributeExists("clases"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void procesarRenovacion_DebeRenovarLicenciaYRedirigir() throws Exception {
        // Given
        when(licenciaService.buscarPorId(1L)).thenReturn(licencia);
        when(licenciaService.renovarLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenReturn(licencia);

        // When/Then
        mockMvc.perform(post("/licencias/1/renovar")
                        .param("titularId", "1")
                        .param("clase", "B")
                        .param("observaciones", "Renovación de prueba")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/licencias/1/imprimirLicencia"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void nuevaLicenciaDuplicado_DebeRetornarFormulario() throws Exception {
        // Given
        when(licenciaService.buscarPorId(1L)).thenReturn(licencia);

        // When/Then
        mockMvc.perform(get("/licencias/duplicar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formDuplicadoLicencia"))
                .andExpect(model().attributeExists("titular"))
                .andExpect(model().attributeExists("licenciaOriginal"))
                .andExpect(model().attributeExists("clases"))
                .andExpect(model().attributeExists("licenciaForm"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listarLicenciasExpiradas_DebeRetornarListado() throws Exception {
        // Given
        when(licenciaService.listarLicenciasExpiradas()).thenReturn(Arrays.asList(licencia));

        // When/Then
        mockMvc.perform(get("/licencias/expiradas"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/licencias-expiradas"))
                .andExpect(model().attributeExists("licenciasExpiradas"))
                .andExpect(model().attributeExists("clases"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void listarLicenciasVigentes_DebeRetornarListado() throws Exception {
        // Given
        when(licenciaService.listarLicenciasVigentes()).thenReturn(Arrays.asList(licencia));

        // When/Then
        mockMvc.perform(get("/licencias/vigentes"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/licencias-vigentes"))
                .andExpect(model().attributeExists("licenciasVigentes"))
                .andExpect(model().attributeExists("grupos"))
                .andExpect(model().attributeExists("factores"));
    }
}
