package com.utn.santafe.gestion_licencias.ControllerTest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utn.santafe.gestion_licencias.controller.TitularController;
import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.TipoDocumento;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.service.LicenciaService;
import com.utn.santafe.gestion_licencias.service.TitularService;

@WebMvcTest(TitularController.class)
class TitularControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TitularService titularService;

    @MockBean
    private LicenciaService licenciaService;

    private Titular testTitular;
    private Titular testTitular2;
    private Licencia testLicencia;

    @BeforeEach
    void setUp() {
        testTitular = new Titular();
        testTitular.setId(1L);
        testTitular.setNombre("Juan");
        testTitular.setApellido("Perez");
        testTitular.setTipoDocumento(TipoDocumento.DNI);
        testTitular.setNumeroDocumento("12345678");
        testTitular.setFechaNacimiento(LocalDate.of(1990, 5, 10)); // Important for age calculations
        testTitular.setDireccion("Calle Falsa 123");
        testTitular.setClaseSolicitada(ClaseLicencia.B);
        testTitular.setGrupoSanguineo(GrupoSanguineo.A);
        testTitular.setFactorRh(FactorRh.POS);
        testTitular.setDonanteOrganos(true);

        testTitular2 = new Titular();
        testTitular2.setId(2L);
        testTitular2.setNombre("Maria");
        testTitular2.setApellido("Gomez");
        testTitular2.setTipoDocumento(TipoDocumento.DNI);
        testTitular2.setNumeroDocumento("87654321");
        testTitular2.setFechaNacimiento(LocalDate.of(1985, 8, 20));
        testTitular2.setDireccion("Avenida Sarmiento 742");
        testTitular2.setClaseSolicitada(ClaseLicencia.A);
        testTitular2.setGrupoSanguineo(GrupoSanguineo.O);
        testTitular2.setFactorRh(FactorRh.NEG);
        testTitular2.setDonanteOrganos(false);

        testLicencia = new Licencia();
        testLicencia.setId(100L);
        testLicencia.setTitular(testTitular);
        testLicencia.setClase(ClaseLicencia.A);
        testLicencia.setFechaEmision(LocalDate.now());
        testLicencia.setFechaVencimiento(LocalDate.now().plusYears(5));
        testLicencia.setObservaciones("Ninguna");
    }

    @Test
    void mostrarFormularioAlta() throws Exception {
        mockMvc.perform(get("/titulares/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeExists("titular", "tiposDoc", "clasesLicencia", "grupos", "factores"))
                .andExpect(model().attribute("titular", new Titular()))
                .andExpect(model().attribute("tiposDoc", TipoDocumento.values()))
                .andExpect(model().attribute("clasesLicencia", ClaseLicencia.values()))
                .andExpect(model().attribute("grupos", GrupoSanguineo.values()))
                .andExpect(model().attribute("factores", FactorRh.values()));
    }

    @Test
    void guardar_ValidarTitular() throws Exception {
        when(titularService.guardarTitular(any(Titular.class))).thenReturn(testTitular);

        mockMvc.perform(post("/titulares/guardar")
                .flashAttr("titular", testTitular))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));

        verify(titularService, times(1)).guardarTitular(any(Titular.class));
    }

    @Test
    void guardar_InvalidarTitular() throws Exception {
        Titular invalidTitular = new Titular();
        invalidTitular.setId(null);

        mockMvc.perform(post("/titulares/guardar")
                .flashAttr("titular", invalidTitular))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeHasErrors("titular"))
                .andExpect(model().attributeExists("titular", "tiposDoc", "clasesLicencia", "grupos", "factores"));
        verify(titularService, never()).guardarTitular(any(Titular.class));
    }

    @Test
    void guardar_TipoDocumentoDesaparecido() throws Exception {
        Titular titularTipoDocDesap = new Titular();
        titularTipoDocDesap.setNombre("Leandro");
        titularTipoDocDesap.setApellido("Perez");
        titularTipoDocDesap.setNumeroDocumento("12345");
        titularTipoDocDesap.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titularTipoDocDesap.setDireccion("Av ramirez 123");
        titularTipoDocDesap.setClaseSolicitada(ClaseLicencia.A);
        titularTipoDocDesap.setGrupoSanguineo(GrupoSanguineo.AB);
        titularTipoDocDesap.setFactorRh(FactorRh.NEG);
        titularTipoDocDesap.setDonanteOrganos(true);

        mockMvc.perform(post("/titulares/guardar")
                .flashAttr("titular", titularTipoDocDesap))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeHasFieldErrors("titular", "tipoDocumento"));

        verify(titularService, never()).guardarTitular(any(Titular.class));
    }

    @Test
    void guardar_NumeroDocumentoInv() throws Exception {
        Titular titularInvalidDocNum = new Titular();
        titularInvalidDocNum.setNombre("Hector");
        titularInvalidDocNum.setApellido("Roma");
        titularInvalidDocNum.setTipoDocumento(TipoDocumento.DNI);
        titularInvalidDocNum.setNumeroDocumento("INVALID_NUMBER_TOO_LONG_OR_CHARS");
        titularInvalidDocNum.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titularInvalidDocNum.setDireccion("Calle colon");
        titularInvalidDocNum.setClaseSolicitada(ClaseLicencia.A);
        titularInvalidDocNum.setGrupoSanguineo(GrupoSanguineo.AB);
        titularInvalidDocNum.setFactorRh(FactorRh.NEG);
        titularInvalidDocNum.setDonanteOrganos(true);

        mockMvc.perform(post("/titulares/guardar")
                .flashAttr("titular", titularInvalidDocNum))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeHasFieldErrors("titular", "numeroDocumento"));

        verify(titularService, never()).guardarTitular(any(Titular.class));
    }

    @Test
    void listarTitulares() throws Exception {
        List<Titular> titulares = Arrays.asList(testTitular, testTitular2);
        when(titularService.listarTitulares()).thenReturn(titulares);

        mockMvc.perform(get("/titulares/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/lista"))
                .andExpect(model().attributeExists("titulares"))
                .andExpect(model().attribute("titulares", titulares));

        verify(titularService, times(1)).listarTitulares();
    }

    @Test
    void listarTitulares_NoTitulares() throws Exception {
        when(titularService.listarTitulares()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/titulares/lista"))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/lista"))
                .andExpect(model().attributeExists("titulares"))
                .andExpect(model().attribute("titulares", Collections.emptyList()));

        verify(titularService, times(1)).listarTitulares();
    }

    @Test
    void editar_ValidarId() throws Exception {
        when(titularService.buscarPorId(1L)).thenReturn(testTitular);

        mockMvc.perform(get("/titulares/editar/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/formulario"))
                .andExpect(model().attributeExists("titular", "tiposDoc", "clasesLicencia", "grupos", "factores"))
                .andExpect(model().attribute("titular", testTitular));

        verify(titularService, times(1)).buscarPorId(1L);
    }

    @Test
    void editar_InvalidarId() throws Exception {
        when(titularService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/titulares/editar/{id}", 99L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));

        verify(titularService, times(1)).buscarPorId(99L);
    }

    @Test
    void confirmarEliminar_ValidarId() throws Exception {
        when(titularService.buscarPorId(1L)).thenReturn(testTitular);

        mockMvc.perform(get("/titulares/eliminar/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("titulares/confirmar-eliminar"))
                .andExpect(model().attributeExists("titular"))
                .andExpect(model().attribute("titular", testTitular));

        verify(titularService, times(1)).buscarPorId(1L);
    }

    @Test
    void confirmarEliminar_InvalidarId() throws Exception {
        when(titularService.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/titulares/eliminar/{id}", 99L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));

        verify(titularService, times(1)).buscarPorId(99L);
    }

    @Test
    void eliminar_ValidarId() throws Exception {
        doNothing().when(titularService).eliminarPorId(1L);

        mockMvc.perform(post("/titulares/eliminar/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/titulares/lista"));

        verify(titularService, times(1)).eliminarPorId(1L);
    }

    @Test
    void listarHistorialPorTitular_ValidaritularId() throws Exception {
        Licencia licenciaHistorial = new Licencia();
        licenciaHistorial.setId(200L);
        licenciaHistorial.setClase(ClaseLicencia.B);
        licenciaHistorial.setTitular(testTitular);
        licenciaHistorial.setFechaEmision(LocalDate.of(2020, 1, 1));
        licenciaHistorial.setFechaVencimiento(LocalDate.of(2025, 1, 1));
        licenciaHistorial.setObservaciones("Historial");

        when(licenciaService.listarHistorialPorTitular(1L)).thenReturn(Arrays.asList(licenciaHistorial, testLicencia));

        mockMvc.perform(get("/titulares/titular/{titularId}/historial", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("Titulares/historialTitular"))
                .andExpect(model().attributeExists("historialLicencias"))
                .andExpect(model().attribute("historialLicencias", Arrays.asList(licenciaHistorial, testLicencia)));

        verify(licenciaService, times(1)).listarHistorialPorTitular(1L);
    }

    @Test
    void listarHistorialPorTitular_NoLicencias() throws Exception {
        when(licenciaService.listarHistorialPorTitular(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/titulares/titular/{titularId}/historial", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("Titulares/historialTitular"))
                .andExpect(model().attributeExists("historialLicencias"))
                .andExpect(model().attribute("historialLicencias", Collections.emptyList()));

        verify(licenciaService, times(1)).listarHistorialPorTitular(1L);
    }

    @Test
    void listarHistorialPorTitular_InvalidarTitularId() throws Exception {
        when(licenciaService.listarHistorialPorTitular(99L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Titular no encontrado con ID 99"));

        mockMvc.perform(get("/titulares/titular/{titularId}/historial", 99L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Titular no encontrado con ID 99"));

    }
}
