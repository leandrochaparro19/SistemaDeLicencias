package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.auditoria.AuditoriaUsuario;
import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuditoriaUsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuditoriaUsuarioRepository auditoriaRepository;

    private AuditoriaUsuario auditoria1;
    private AuditoriaUsuario auditoria2;

    @BeforeEach
    void setUp() {
        // Crear registros de auditoría de prueba
        auditoria1 = new AuditoriaUsuario();
        auditoria1.setUsuarioModificadoId(1L);
        auditoria1.setUsuarioModificadoDni("12345678");
        auditoria1.setUsuarioQueModificaId(2L);
        auditoria1.setUsuarioQueModificaDni("87654321");
        auditoria1.setCampoModificado("nombre");
        auditoria1.setValorAnterior("Juan");
        auditoria1.setValorNuevo("Juan Carlos");
        auditoria1.setTipoOperacion(TipoOperacion.MODIFICACION);
        auditoria1.setIpOrigen("192.168.1.1");
        auditoria1.setFechaModificacion(LocalDateTime.now().minusDays(1));
        entityManager.persist(auditoria1);

        auditoria2 = new AuditoriaUsuario();
        auditoria2.setUsuarioModificadoId(1L);
        auditoria2.setUsuarioModificadoDni("12345678");
        auditoria2.setUsuarioQueModificaId(3L);
        auditoria2.setUsuarioQueModificaDni("11223344");
        auditoria2.setCampoModificado("activo");
        auditoria2.setValorAnterior("true");
        auditoria2.setValorNuevo("false");
        auditoria2.setTipoOperacion(TipoOperacion.DESACTIVACION);
        auditoria2.setIpOrigen("192.168.1.2");
        auditoria2.setFechaModificacion(LocalDateTime.now());
        entityManager.persist(auditoria2);

        entityManager.flush();
    }

    @Test
    void findByUsuarioModificadoIdOrderByFechaModificacionDesc_DebeRetornarRegistrosOrdenados() {
        // When
        List<AuditoriaUsuario> registros = auditoriaRepository.findByUsuarioModificadoIdOrderByFechaModificacionDesc(1L);

        // Then
        assertThat(registros).hasSize(2);
        assertThat(registros.get(0).getFechaModificacion()).isAfter(registros.get(1).getFechaModificacion());
    }

    @Test
    void findByUsuarioQueModificaIdOrderByFechaModificacionDesc_DebeRetornarRegistros() {
        // When
        List<AuditoriaUsuario> registros = auditoriaRepository.findByUsuarioQueModificaIdOrderByFechaModificacionDesc(2L);

        // Then
        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getUsuarioModificadoDni()).isEqualTo("12345678");
    }

    @Test
    void findByTipoOperacionOrderByFechaModificacionDesc_DebeRetornarRegistros() {
        // When
        List<AuditoriaUsuario> registros = auditoriaRepository.findByTipoOperacionOrderByFechaModificacionDesc(TipoOperacion.DESACTIVACION);

        // Then
        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getCampoModificado()).isEqualTo("activo");
    }

    @Test
    void findByFechaModificacionBetween_DebeRetornarRegistros() {
        // When
        List<AuditoriaUsuario> registros = auditoriaRepository.findByFechaModificacionBetween(
                LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(1));

        // Then
        assertThat(registros).hasSize(2);
    }

    @Test
    void findByUsuarioModificadoDni_DebeRetornarRegistros() {
        // When
        List<AuditoriaUsuario> registros = auditoriaRepository.findByUsuarioModificadoDni("12345678");

        // Then
        assertThat(registros).hasSize(2);
    }
}
