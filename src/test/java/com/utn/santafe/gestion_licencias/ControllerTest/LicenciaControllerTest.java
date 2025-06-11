package com.utn.santafe.gestion_licencias.ControllerTest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import com.utn.santafe.gestion_licencias.controller.LicenciaController;
import com.utn.santafe.gestion_licencias.model.form.LicenciaForm;
import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.TipoDocumento;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import com.utn.santafe.gestion_licencias.service.LicenciaService;

@WebMvcTest(LicenciaController.class)
class LicenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private TitularRepository titularRepository;
    @InjectMocks
    private LicenciaService licenciaService;

    private Titular testTitular;
    private Licencia testLicencia;

    @BeforeEach
    void setUp() {
        testTitular = new Titular();
        testTitular.setId(1L);
        testTitular.setNombre("Juan");
        testTitular.setApellido("Perez");
        testTitular.setTipoDocumento(TipoDocumento.DNI);
        testTitular.setNumeroDocumento("12345678");
        testTitular.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        testTitular.setDireccion("Calle Sur 123");
        testTitular.setClaseSolicitada(ClaseLicencia.B);
        testTitular.setGrupoSanguineo(GrupoSanguineo.A);
        testTitular.setFactorRh(FactorRh.POS);
        testTitular.setDonanteOrganos(true);

        testLicencia = new Licencia();
        testLicencia.setId(100L);
        testLicencia.setTitular(testTitular);
        testLicencia.setClase(ClaseLicencia.A);
        testLicencia.setFechaEmision(LocalDate.now());
        testLicencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        testLicencia.setUsuarioAdmin("admin");
        testLicencia.setObservaciones("Ninguna");
        testLicencia.setVigente(true);
        testLicencia.setTipoEmision(tipoEmision.ORIGINAL);
    }

    @Test
    void nuevaLicencia_ValidarTitularId() throws Exception {
        when(titularRepository.findById(1L)).thenReturn(Optional.of(testTitular));

        mockMvc.perform(get("/licencias/nueva/{titularId}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/form"))
                .andExpect(model().attributeExists("titular", "clases", "licenciaForm"))
                .andExpect(model().attribute("titular", testTitular))
                .andExpect(model().attribute("clases", ClaseLicencia.values()))
                .andExpect(model().attribute("licenciaForm", new LicenciaForm(10L, ClaseLicencia.A, "Usa anteojos")));

        verify(titularRepository, times(1)).findById(100L);
    }

    @Test
    void nuevaLicencia_InvalidarTitularId() throws Exception {
        when(titularRepository.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/licencias/nueva/{titularId}", 2L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("El titular no existe"));
    }

    @Test
    void guardarLicencia_ValidaForma() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.A, "No ve de un ojo");
        when(licenciaService.emitirLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenReturn(testLicencia);

        mockMvc.perform(post("/licencias/guardar")
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Licencia emitida ID " + testLicencia.getId()));

        verify(licenciaService, times(1)).emitirLicencia(
                validForm.getTitularId(),
                validForm.getClase(),
                validForm.getObservaciones(),
                "admin");
    }

    @Test
    void guardarLicencia_InvalidaForma() throws Exception {
        LicenciaForm invalidForm = new LicenciaForm(1L, null, "");
        when(titularRepository.findById(1L)).thenReturn(Optional.of(testTitular));

        mockMvc.perform(post("/licencias/guardar")
                .flashAttr("licenciaForm", invalidForm))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/form"))
                .andExpect(model().attributeHasErrors("licenciaForm"))
                .andExpect(model().attributeExists("titular", "clases"));

        verify(titularRepository, times(1)).findById(1L);
        verify(licenciaService, never()).emitirLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString());
    }

    @Test
    void guardarLicencia() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.A, "Donador");
        when(licenciaService.emitirLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Error al emitir licencia"));

        mockMvc.perform(post("/licencias/guardar")
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"))
                .andExpect(flash().attributeExists("error"))
                .andExpect(flash().attribute("error", "Error al emitir licencia"));

        verify(licenciaService, times(1)).emitirLicencia(
                validForm.getTitularId(),
                validForm.getClase(),
                validForm.getObservaciones(),
                "admin");
    }

    @Test
    void nuevaLicenciaDuplicado_ValidarLicenciaId() throws Exception {
        when(licenciaService.buscarPorId(100L)).thenReturn(testLicencia);
        when(titularRepository.findById(1L)).thenReturn(Optional.of(testTitular));

        mockMvc.perform(get("/licencias/duplicar/{licenciaId}", 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formDuplicadoLicencia"))
                .andExpect(model().attributeExists("titular", "licenciaOriginal", "clases", "licenciaForm"))
                .andExpect(model().attribute("titular", testTitular))
                .andExpect(model().attribute("licenciaOriginal", testLicencia))
                .andExpect(model().attribute("clases", ClaseLicencia.values()))
                .andExpect(model().attribute("licenciaForm", new LicenciaForm(1L, ClaseLicencia.D, "Ninguna")));

        verify(licenciaService, times(1)).buscarPorId(100L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if original license ID is invalid for duplication")
    void nuevaLicenciaDuplicado_InvalidLicenseId_ThrowsEntityNotFoundException() throws Exception {
        when(licenciaService.buscarPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/licencias/duplicar/{licenciaId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("La licencia original no existe"));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if titular associated with original license is null for duplication")
    void nuevaLicenciaDuplicado_NullTitularInOriginalLicense_ThrowsEntityNotFoundException() throws Exception {
        Licencia licenciaWithoutTitular = new Licencia();
        licenciaWithoutTitular.setId(101L);
        licenciaWithoutTitular.setTitular(null);

        when(licenciaService.buscarPorId(101L)).thenReturn(licenciaWithoutTitular);

        mockMvc.perform(get("/licencias/duplicar/{licenciaId}", 101L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("El titular asociado a la licencia no existe"));
    }

    @Test
    void guardarDuplicadoLicencia_ValidaForma() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.B, "No donante");
        Licencia duplicatedLicencia = new Licencia();
        duplicatedLicencia.setId(200L);
        duplicatedLicencia.setTitular(testTitular);
        duplicatedLicencia.setClase(validForm.getClase());
        duplicatedLicencia.setObservaciones(validForm.getObservaciones());
        duplicatedLicencia.setFechaEmision(LocalDate.now());
        duplicatedLicencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        duplicatedLicencia.setUsuarioAdmin("admin");
        duplicatedLicencia.setVigente(true);
        duplicatedLicencia.setTipoEmision(tipoEmision.DUPLICADO);

        when(licenciaService.emitirDuplicado(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenReturn(duplicatedLicencia);

        mockMvc.perform(post("/licencias/guardarDuplicado")
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(
                        flash().attribute("success", "Duplicado de licencia emitido ID " + duplicatedLicencia.getId()));

        verify(licenciaService, times(1)).emitirDuplicado(
                validForm.getTitularId(),
                validForm.getClase(),
                validForm.getObservaciones(),
                "admin");
    }

    @Test
    void guardarDuplicadoLicencia_InvalidaForma() throws Exception {
        LicenciaForm invalidForm = new LicenciaForm(1L, null, "");
        when(titularRepository.findById(1L)).thenReturn(Optional.of(testTitular));

        mockMvc.perform(post("/licencias/guardarDuplicado")
                .flashAttr("licenciaForm", invalidForm))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formDuplicado")) // Your controller returns "formDuplicado"
                .andExpect(model().attributeHasErrors("licenciaForm"))
                .andExpect(model().attributeExists("titular", "clases"));

        verify(titularRepository, times(1)).findById(1L);
        verify(licenciaService, never()).emitirDuplicado(anyLong(), any(ClaseLicencia.class), anyString(), anyString());
    }

    @Test
    void guardarDuplicadoLicencia() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.A, "Usa anteojos");
        when(licenciaService.emitirDuplicado(anyLong(), any(ClaseLicencia.class), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Error al emitir duplicado"));
        when(titularRepository.findById(1L)).thenReturn(Optional.of(testTitular));

        mockMvc.perform(post("/licencias/guardarDuplicado")
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formDuplicadoLicencia")) // Your controller returns
                                                                           // "formDuplicadoLicencia"
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Error al emitir duplicado"))
                .andExpect(model().attributeExists("titular", "clases"));

        verify(licenciaService, times(1)).emitirDuplicado(
                validForm.getTitularId(),
                validForm.getClase(),
                validForm.getObservaciones(),
                "admin");
        verify(titularRepository, times(1)).findById(1L);
    }

    @Test
    void verListadoLicencias() throws Exception {
        Licencia anotherLicencia = new Licencia();
        anotherLicencia.setId(101L);
        anotherLicencia.setTitular(testTitular);
        anotherLicencia.setClase(ClaseLicencia.B);
        anotherLicencia.setFechaEmision(LocalDate.now());
        anotherLicencia.setFechaVencimiento(LocalDate.now().plusYears(3));
        anotherLicencia.setUsuarioAdmin("admin");
        anotherLicencia.setVigente(true);
        anotherLicencia.setTipoEmision(tipoEmision.ORIGINAL);

        when(licenciaService.listarLicencias()).thenReturn(Arrays.asList(testLicencia, anotherLicencia));

        mockMvc.perform(get("/licencias/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/lista"))
                .andExpect(model().attributeExists("licencias"))
                .andExpect(model().attribute("licencias", Arrays.asList(testLicencia, anotherLicencia)));

        verify(licenciaService, times(1)).listarLicencias();
    }

    @Test
    void verListadoLicencias_listaVacia() throws Exception {
        when(licenciaService.listarLicencias()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/licencias/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/lista"))
                .andExpect(model().attributeExists("licencias"))
                .andExpect(model().attribute("licencias", Collections.emptyList()));

        verify(licenciaService, times(1)).listarLicencias();
    }

    @Test
    void imprimirLicencia_ValidaId() throws Exception {
        when(licenciaService.buscarPorId(100L)).thenReturn(testLicencia);

        mockMvc.perform(get("/licencias/{id}/imprimirLicencia", 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/imprimirLicencia"))
                .andExpect(model().attributeExists("licencia"))
                .andExpect(model().attribute("licencia", testLicencia));

        verify(licenciaService, times(1)).buscarPorId(100L);
    }

    @Test
    void imprimirLicencia_InvalidarId() throws Exception {
        when(licenciaService.buscarPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/licencias/{id}/imprimirLicencia", 999L))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/imprimirLicencia"))
                .andExpect(model().attribute("licencia", null));

        verify(licenciaService, times(1)).buscarPorId(999L);
    }

    @Test
    void mostrarFormularioRenovar_ValidarId() throws Exception {
        when(licenciaService.buscarPorId(100L)).thenReturn(testLicencia);

        mockMvc.perform(get("/licencias/{id}/renovar", 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formRenovar"))
                .andExpect(model().attributeExists("licenciaActual", "licenciaForm", "clases"))
                .andExpect(model().attribute("licenciaActual", testLicencia))
                .andExpect(model().attribute("clases", ClaseLicencia.values()))
                .andExpect(model().attribute("licenciaForm",
                        new LicenciaForm(testTitular.getId(), ClaseLicencia.A, "Ninguna")));

        verify(licenciaService, times(1)).buscarPorId(100L);
    }

    @Test
    void mostrarFormularioRenovar_InvalidarId() throws Exception {
        when(licenciaService.buscarPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/licencias/{id}/renovar", 999L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/licencias?error=LicenciaNoEncontrada"));

        verify(licenciaService, times(1)).buscarPorId(999L);
    }

    @Test
    void procesarRenovacion_ValidarForma() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.B, "Renovada con cambios");
        Licencia renewedLicencia = new Licencia();
        renewedLicencia.setId(201L);
        renewedLicencia.setTitular(testTitular);
        renewedLicencia.setClase(validForm.getClase());
        renewedLicencia.setObservaciones(validForm.getObservaciones());
        renewedLicencia.setFechaEmision(LocalDate.now());
        renewedLicencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        renewedLicencia.setUsuarioAdmin("admin");
        renewedLicencia.setVigente(true);
        renewedLicencia.setTipoEmision(tipoEmision.RENOVACION);

        when(licenciaService.buscarPorId(100L)).thenReturn(testLicencia);
        when(licenciaService.renovarLicencia(
                eq(testLicencia.getId()), any(ClaseLicencia.class), anyString(), anyString()))
                .thenReturn(renewedLicencia);

        mockMvc.perform(post("/licencias/{id}/renovar", 100L)
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/licencias/" + renewedLicencia.getId() + "/imprimirLicencia"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Licencia renovada (ID: " + renewedLicencia.getId() + ")"));

        verify(licenciaService, times(1)).buscarPorId(100L);
        verify(licenciaService, times(1)).renovarLicencia(
                testLicencia.getId(),
                validForm.getClase(),
                validForm.getObservaciones(),
                String.valueOf(validForm.getTitularId()));
    }

    @Test
    void procesarRenovacion_InvalidaForma() throws Exception {
        LicenciaForm invalidForm = new LicenciaForm(1L, null, "");
        when(licenciaService.buscarPorId(100L)).thenReturn(testLicencia);

        mockMvc.perform(post("/licencias/{id}/renovar", 100L)
                .flashAttr("licenciaForm", invalidForm))
                .andExpect(status().isOk())
                .andExpect(view().name("licencias/formRenovar"))
                .andExpect(model().attributeHasErrors("licenciaForm"))
                .andExpect(model().attributeExists("licenciaActual", "clases"));

        verify(licenciaService, times(1)).buscarPorId(100L);
        verify(licenciaService, never()).renovarLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString());
    }

    @Test
    void procesarRenovacion_InvalidarId() throws Exception {
        LicenciaForm validForm = new LicenciaForm(1L, ClaseLicencia.A, "No usa anteojos");
        when(licenciaService.buscarPorId(999L)).thenReturn(null);

        mockMvc.perform(post("/licencias/{id}/renovar", 999L)
                .flashAttr("licenciaForm", validForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/licencias?error=LicenciaNoEncontrada"));

        verify(licenciaService, times(1)).buscarPorId(999L);
        verify(licenciaService, never()).renovarLicencia(anyLong(), any(ClaseLicencia.class), anyString(), anyString());
    }
}
