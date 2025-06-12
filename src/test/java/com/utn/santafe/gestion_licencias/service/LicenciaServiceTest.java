package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LicenciaServiceTest {

    @Mock
    private LicenciaRepository licenciaRepository;

    @Mock
    private TitularRepository titularRepository;

    @InjectMocks
    private LicenciaService licenciaService;

    private Titular titular;
    private Licencia licenciaVigente;
    private Licencia licenciaExpirada;

    @BeforeEach
    void setUp() {
        // Crear un titular
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

        // Crear una licencia vigente
        licenciaVigente = new Licencia();
        licenciaVigente.setId(1L);
        licenciaVigente.setTitular(titular);
        licenciaVigente.setClase(ClaseLicencia.B);
        licenciaVigente.setFechaEmision(LocalDate.now().minusMonths(1));
        licenciaVigente.setFechaVencimiento(LocalDate.now().plusYears(5));
        licenciaVigente.setUsuarioAdmin("admin");
        licenciaVigente.setObservaciones("Licencia vigente");
        licenciaVigente.setVigente(true);
        licenciaVigente.setTipoEmision(tipoEmision.ORIGINAL);
        licenciaVigente.setCosto(new BigDecimal("48.00"));

        // Crear una licencia expirada
        licenciaExpirada = new Licencia();
        licenciaExpirada.setId(2L);
        licenciaExpirada.setTitular(titular);
        licenciaExpirada.setClase(ClaseLicencia.A);
        licenciaExpirada.setFechaEmision(LocalDate.now().minusYears(6));
        licenciaExpirada.setFechaVencimiento(LocalDate.now().minusYears(1));
        licenciaExpirada.setUsuarioAdmin("admin");
        licenciaExpirada.setObservaciones("Licencia expirada");
        licenciaExpirada.setVigente(false);
        licenciaExpirada.setTipoEmision(tipoEmision.ORIGINAL);
        licenciaExpirada.setCosto(new BigDecimal("40.00"));
    }

    @Test
    void emitirLicencia_DebeEmitirLicenciaCorrectamente() {
        // Given
        when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(licenciaRepository.countByTitularAndClaseAndVigente(any(), any(), anyBoolean())).thenReturn(0L);
        when(licenciaRepository.findByTitularAndVigente(any(), anyBoolean())).thenReturn(Collections.emptyList());
        when(licenciaRepository.save(any())).thenAnswer(invocation -> {
            Licencia lic = invocation.getArgument(0);
            lic.setId(3L);
            return lic;
        });

        // When
        Licencia result = licenciaService.emitirLicencia(1L, ClaseLicencia.B, "Observaciones de prueba", "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitular()).isEqualTo(titular);
        assertThat(result.getClase()).isEqualTo(ClaseLicencia.B);
        assertThat(result.getObservaciones()).isEqualTo("Observaciones de prueba");
        assertThat(result.getUsuarioAdmin()).isEqualTo("admin");
        assertThat(result.isVigente()).isTrue();
        assertThat(result.getTipoEmision()).isEqualTo(tipoEmision.ORIGINAL);
        verify(licenciaRepository).save(any());
    }

    @Test
    void emitirLicencia_DebeArrojarExcepcionSiTitularNoExiste() {
        // Given
        when(titularRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> licenciaService.emitirLicencia(999L, ClaseLicencia.B, "Observaciones", "admin"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listarLicencias_DebeRetornarTodasLasLicencias() {
        // Given
        when(licenciaRepository.findAll()).thenReturn(Arrays.asList(licenciaVigente, licenciaExpirada));

        // When
        List<Licencia> result = licenciaService.listarLicencias();

        // Then
        assertThat(result).hasSize(2);
        verify(licenciaRepository).findAll();
    }

    @Test
    void buscarPorId_DebeRetornarLicencia() {
        // Given
        when(licenciaRepository.findById(1L)).thenReturn(Optional.of(licenciaVigente));

        // When
        Licencia result = licenciaService.buscarPorId(1L);

        // Then
        assertThat(result).isEqualTo(licenciaVigente);
        verify(licenciaRepository).findById(1L);
    }

    @Test
    void buscarPorId_DebeArrojarExcepcionSiLicenciaNoExiste() {
        // Given
        when(licenciaRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> licenciaService.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void renovarLicencia_DebeRenovarLicenciaCorrectamente() {
        // Given
        when(licenciaRepository.findById(1L)).thenReturn(Optional.of(licenciaVigente));
        when(licenciaRepository.findByTitularAndVigente(any(), anyBoolean())).thenReturn(Collections.singletonList(licenciaVigente));
        when(licenciaRepository.countByTitularAndClaseAndVigente(any(), any(), anyBoolean())).thenReturn(1L);
        when(licenciaRepository.save(any())).thenAnswer(invocation -> {
            Licencia lic = invocation.getArgument(0);
            lic.setId(3L);
            return lic;
        });

        // When
        Licencia result = licenciaService.renovarLicencia(1L, ClaseLicencia.B, "Renovación", "admin");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitular()).isEqualTo(titular);
        assertThat(result.getClase()).isEqualTo(ClaseLicencia.B);
        assertThat(result.getObservaciones()).isEqualTo("Renovación");
        assertThat(result.getUsuarioAdmin()).isEqualTo("admin");
        assertThat(result.isVigente()).isTrue();
        assertThat(result.getTipoEmision()).isEqualTo(tipoEmision.RENOVACION);
        verify(licenciaRepository).save(any());
    }

    @Test
    void listarHistorialPorTitular_DebeRetornarHistorialDeLicencias() {
        // Given
        when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));
        when(licenciaRepository.findByTitularOrderByFechaEmisionDesc(titular))
                .thenReturn(Arrays.asList(licenciaVigente, licenciaExpirada));

        // When
        List<Licencia> result = licenciaService.listarHistorialPorTitular(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(licenciaRepository).findByTitularOrderByFechaEmisionDesc(titular);
    }

    @Test
    void listarLicenciasExpiradas_DebeRetornarLicenciasExpiradas() {
        // Given
        when(licenciaRepository.findByFechaVencimientoBeforeOrderByFechaVencimientoDesc(any()))
                .thenReturn(Collections.singletonList(licenciaExpirada));

        // When
        List<Licencia> result = licenciaService.listarLicenciasExpiradas();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(licenciaExpirada.getId());
        verify(licenciaRepository).findByFechaVencimientoBeforeOrderByFechaVencimientoDesc(any());
    }

    @Test
    void listarLicenciasVigentes_DebeRetornarLicenciasVigentes() {
        // Given
        when(licenciaRepository.findByVigenteTrue())
                .thenReturn(Collections.singletonList(licenciaVigente));

        // When
        List<Licencia> result = licenciaService.listarLicenciasVigentes();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(licenciaVigente.getId());
        verify(licenciaRepository).findByVigenteTrue();
    }
}
