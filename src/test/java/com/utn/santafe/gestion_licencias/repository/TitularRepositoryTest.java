package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.titular.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TitularRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TitularRepository titularRepository;

    private Titular titular1;
    private Titular titular2;

    @BeforeEach
    void setUp() {
        // Crear titulares de prueba
        titular1 = new Titular();
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
        entityManager.persist(titular1);

        titular2 = new Titular();
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
        entityManager.persist(titular2);

        entityManager.flush();
    }

    @Test
    void findById_DebeRetornarTitular() {
        // When
        Optional<Titular> found = titularRepository.findById(titular1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNumeroDocumento()).isEqualTo("12345678");
    }

    @Test
    void findAll_DebeRetornarTodosLosTitulares() {
        // When
        List<Titular> titulares = titularRepository.findAll();

        // Then
        assertThat(titulares).hasSize(2);
    }

    @Test
    void save_DebeGuardarNuevoTitular() {
        // Given
        Titular nuevoTitular = new Titular();
        nuevoTitular.setTipoDocumento(TipoDocumento.DNI);
        nuevoTitular.setNumeroDocumento("11223344");
        nuevoTitular.setApellido("Rodríguez");
        nuevoTitular.setNombre("Carlos");
        nuevoTitular.setFechaNacimiento(LocalDate.of(1990, 10, 10));
        nuevoTitular.setDireccion("Calle Principal 456");
        nuevoTitular.setClaseSolicitada(ClaseLicencia.C);
        nuevoTitular.setGrupoSanguineo(GrupoSanguineo.B);
        nuevoTitular.setFactorRh(FactorRh.POS);
        nuevoTitular.setDonanteOrganos(true);

        // When
        Titular saved = titularRepository.save(nuevoTitular);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(titularRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void deleteById_DebeEliminarTitular() {
        // When
        titularRepository.deleteById(titular1.getId());
        entityManager.flush();

        // Then
        assertThat(titularRepository.findById(titular1.getId())).isEmpty();
    }
}
