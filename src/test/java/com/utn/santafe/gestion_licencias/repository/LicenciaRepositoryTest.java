package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LicenciaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LicenciaRepository licenciaRepository;

    private Titular titular;
    private Licencia licenciaVigente;
    private Licencia licenciaExpirada;

    @BeforeEach
    void setUp() {
        // Crear un titular
        titular = new Titular();
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
        entityManager.persist(titular);

        // Crear una licencia vigente
        licenciaVigente = new Licencia();
        licenciaVigente.setTitular(titular);
        licenciaVigente.setClase(ClaseLicencia.B);
        licenciaVigente.setFechaEmision(LocalDate.now().minusMonths(1));
        licenciaVigente.setFechaVencimiento(LocalDate.now().plusYears(5));
        licenciaVigente.setUsuarioAdmin("admin");
        licenciaVigente.setObservaciones("Licencia vigente");
        licenciaVigente.setVigente(true);
        licenciaVigente.setTipoEmision(tipoEmision.ORIGINAL);
        licenciaVigente.setCosto(new BigDecimal("48.00"));
        entityManager.persist(licenciaVigente);

        // Crear una licencia expirada
        licenciaExpirada = new Licencia();
        licenciaExpirada.setTitular(titular);
        licenciaExpirada.setClase(ClaseLicencia.A);
        licenciaExpirada.setFechaEmision(LocalDate.now().minusYears(6));
        licenciaExpirada.setFechaVencimiento(LocalDate.now().minusYears(1));
        licenciaExpirada.setUsuarioAdmin("admin");
        licenciaExpirada.setObservaciones("Licencia expirada");
        licenciaExpirada.setVigente(false);
        licenciaExpirada.setTipoEmision(tipoEmision.ORIGINAL);
        licenciaExpirada.setCosto(new BigDecimal("40.00"));
        entityManager.persist(licenciaExpirada);

        entityManager.flush();
    }

    @Test
    void findByTitularAndClaseAndVigente_DebeRetornarLicenciaVigente() {
        // When
        Optional<Licencia> found = licenciaRepository.findByTitularAndClaseAndVigente(titular, ClaseLicencia.B, true);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(licenciaVigente.getId());
    }

    @Test
    void existsByTitularAndClaseAndFechaEmisionBefore_DebeRetornarTrue() {
        // When
        boolean exists = licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                titular, ClaseLicencia.A, LocalDate.now());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void countByTitularAndClaseAndVigente_DebeRetornarCantidadCorrecta() {
        // When
        long count = licenciaRepository.countByTitularAndClaseAndVigente(titular, ClaseLicencia.B, true);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByTitularAndVigente_DebeRetornarLicenciasVigentes() {
        // When
        List<Licencia> licencias = licenciaRepository.findByTitularAndVigente(titular, true);

        // Then
        assertThat(licencias).hasSize(1);
        assertThat(licencias.get(0).getId()).isEqualTo(licenciaVigente.getId());
    }

    @Test
    void findByTitularOrderByFechaEmisionDesc_DebeRetornarLicenciasOrdenadas() {
        // When
        List<Licencia> licencias = licenciaRepository.findByTitularOrderByFechaEmisionDesc(titular);

        // Then
        assertThat(licencias).hasSize(2);
        assertThat(licencias.get(0).getFechaEmision()).isAfter(licencias.get(1).getFechaEmision());
    }

    @Test
    void findByFechaVencimientoBeforeOrderByFechaVencimientoDesc_DebeRetornarLicenciasExpiradas() {
        // When
        List<Licencia> licencias = licenciaRepository.findByFechaVencimientoBeforeOrderByFechaVencimientoDesc(LocalDate.now());

        // Then
        assertThat(licencias).hasSize(1);
        assertThat(licencias.get(0).getId()).isEqualTo(licenciaExpirada.getId());
    }

    @Test
    void findByVigenteTrue_DebeRetornarLicenciasVigentes() {
        // When
        List<Licencia> licencias = licenciaRepository.findByVigenteTrue();

        // Then
        assertThat(licencias).hasSize(1);
        assertThat(licencias.get(0).getId()).isEqualTo(licenciaVigente.getId());
    }
}
