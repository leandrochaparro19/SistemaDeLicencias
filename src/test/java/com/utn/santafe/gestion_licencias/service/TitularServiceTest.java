package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.titular.*;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TitularServiceTest {

    @Mock
    private TitularRepository titularRepository;

    @InjectMocks
    private TitularService titularService;

    private Titular titular1;
    private Titular titular2;

    @BeforeEach
    void setUp() {
        // Crear titulares de prueba
        titular1 = new Titular();
        titular1.setId(1L);
        titular1.setTipoDocumento(TipoDocumento.DNI);
        titular1.setNumeroDocumento("12345678");
        titular1.setApellido("Pérez");
        titular1.setNombre("Juan");
        titular1.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        titular1.setDireccion("Calle Falsa 123");
        titular1.setClaseSolicitada(ClaseLicencia.B);
        titular1.setGrupoSanguineo(GrupoSanguineo.A);
        titular1.setFactorRh(FactorRh.POS);
        titular1.setDonanteOrganos(true);

        titular2 = new Titular();
        titular2.setId(2L);
        titular2.setTipoDocumento(TipoDocumento.DNI);
        titular2.setNumeroDocumento("87654321");
        titular2.setApellido("González");
        titular2.setNombre("María");
        titular2.setFechaNacimiento(LocalDate.of(1985, 5, 15));
        titular2.setDireccion("Avenida Siempreviva 742");
        titular2.setClaseSolicitada(ClaseLicencia.A);
        titular2.setGrupoSanguineo(GrupoSanguineo.O);
        titular2.setFactorRh(FactorRh.NEG);
        titular2.setDonanteOrganos(false);
    }

    @Test
    void guardarTitular_DebeGuardarTitular() {
        // Given
        when(titularRepository.save(any(Titular.class))).thenReturn(titular1);

        // When
        Titular result = titularService.guardarTitular(titular1);

        // Then
        assertThat(result).isEqualTo(titular1);
        verify(titularRepository).save(titular1);
    }

    @Test
    void listarTitulares_DebeRetornarTodosLosTitulares() {
        // Given
        when(titularRepository.findAll()).thenReturn(Arrays.asList(titular1, titular2));

        // When
        List<Titular> result = titularService.listarTitulares();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(titular1, titular2);
        verify(titularRepository).findAll();
    }

    @Test
    void buscarPorId_DebeRetornarTitular() {
        // Given
        when(titularRepository.findById(1L)).thenReturn(Optional.of(titular1));

        // When
        Titular result = titularService.buscarPorId(1L);

        // Then
        assertThat(result).isEqualTo(titular1);
        verify(titularRepository).findById(1L);
    }

    @Test
    void buscarPorId_DebeRetornarNullSiTitularNoExiste() {
        // Given
        when(titularRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Titular result = titularService.buscarPorId(999L);

        // Then
        assertThat(result).isNull();
        verify(titularRepository).findById(999L);
    }

    @Test
    void eliminarPorId_DebeEliminarTitular() {
        // When
        titularService.eliminarPorId(1L);

        // Then
        verify(titularRepository).deleteById(1L);
    }
}
