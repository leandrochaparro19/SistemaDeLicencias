package com.utn.santafe.gestion_licencias.RepositoryTest;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.TipoDocumento;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class LicenciaRepositoryTest {

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private TitularRepository titularRepository;

    private Titular titular;
    private Licencia licencia;

    @BeforeEach
    void setUp() {
        titular = new Titular();
        titular.setTipoDocumento(TipoDocumento.DNI);
        titular.setNumeroDocumento("12345678");
        titular.setApellido("Pérez");
        titular.setNombre("Ana");
        titular.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        titular.setDireccion("Av. Siempreviva 742");
        titular.setClaseSolicitada(ClaseLicencia.B);
        titular.setGrupoSanguineo(GrupoSanguineo.A);
        titular.setFactorRh(FactorRh.POS);
        titular.setDonanteOrganos(true);

        titular = titularRepository.save(titular);

        licencia = new Licencia();
        licencia.setTitular(titular);
        licencia.setClase(ClaseLicencia.B);
        licencia.setFechaEmision(LocalDate.now().minusYears(1));
        licencia.setFechaVencimiento(LocalDate.now().plusYears(4));
        licencia.setUsuarioAdmin("admin");
        licencia.setObservaciones("Ninguna");
        licencia.setVigente(true);
        licencia.setTipoEmision(tipoEmision.ORIGINAL);

        licenciaRepository.save(licencia);
    }

    @Test
    void testFindByTitularAndClaseAndVigente() {
        Optional<Licencia> found = licenciaRepository.findByTitularAndClaseAndVigente(titular, ClaseLicencia.B, true);
        assertThat(found).isPresent();
    }

    @Test
    void testExistsByTitularAndClaseAndFechaEmisionBefore() {
        boolean exists = licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                titular, ClaseLicencia.B, LocalDate.now());
        assertThat(exists).isTrue();
    }

    @Test
    void testCountByTitularAndClaseAndVigente() {
        long count = licenciaRepository.countByTitularAndClaseAndVigente(titular, ClaseLicencia.B, true);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByTitularAndVigente() {
        List<Licencia> results = licenciaRepository.findByTitularAndVigente(titular, true);
        assertThat(results).hasSize(1);
    }

    @Test
    void testFindByTitularOrderByFechaEmisionDesc() {
        List<Licencia> results = licenciaRepository.findByTitularOrderByFechaEmisionDesc(titular);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getFechaEmision()).isEqualTo(licencia.getFechaEmision());
    }
}