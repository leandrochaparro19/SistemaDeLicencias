package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TitularServiceTest {

    @InjectMocks
    private TitularService titularService;

    @Mock
    private TitularRepository titularRepository;

    private Titular titular;

    @BeforeEach
    void setUp(){
        titular = new Titular();
        titular.setId(1L);
        titular.setNumeroDocumento("12345678");
        titular.setNombre("Juan");
        titular.setApellido("Perez");
        titular.setFechaNacimiento(LocalDate.of(1990, 1, 1));
    }

    /*    ----------------  guardarTitular  ----------------    */

    @Test
    void guardarTitular_Ok(){
        when(titularRepository.save(titular)).thenReturn(titular);

        Titular guardado = titularService.guardarTitular(titular);

        assertThat(guardado).isSameAs(titular);
        verify(titularRepository).save(titular);
    }

    /*    ----------------  listarTitulares  ----------------    */

    @Test
    void listarTitulares_Ok(){
        List<Titular> titulares = List.of(titular, new Titular());
        when(titularRepository.findAll()).thenReturn(titulares);

        List<Titular> resultado = titularService.listarTitulares();

        assertThat(resultado).containsExactlyElementsOf(titulares);
        verify(titularRepository).findAll();
    }

    /*    ----------------  buscarPorId  ----------------    */

    @Test
    void buscarPorId_Ok(){
        when(titularRepository.findById(1L)).thenReturn(Optional.of(titular));

        Titular resultado = titularService.buscarPorId(1L);
        assertThat(resultado).isSameAs(titular);
        verify(titularRepository).findById(1L);
    }

    @Test
    void buscarPorId_NotFound(){
        when(titularRepository.findById(99L)).thenReturn(Optional.empty());

        Titular resultado = titularService.buscarPorId(99L);

        assertThat(resultado).isNull();
        verify(titularRepository).findById(99L);
    }

    /*    ----------------  eliminarPorId  ----------------    */

    @Test
    void eliminarPorId_Ok(){
        titularService.eliminarPorId(1L);

        verify(titularRepository).deleteById(1L);
    }
}
