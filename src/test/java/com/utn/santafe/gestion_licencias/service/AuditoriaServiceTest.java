package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.auditoria.AuditoriaUsuario;
import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.repository.AuditoriaUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditoriaServiceTest {

    @Mock
    private AuditoriaUsuarioRepository auditoriaRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuarioAnterior;
    private Usuario usuarioNuevo;
    private Usuario usuarioModificador;

    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba
        usuarioAnterior = new Usuario();
        usuarioAnterior.setId(1L);
        usuarioAnterior.setNombre("Juan");
        usuarioAnterior.setApellido("Pérez");
        usuarioAnterior.setDni("12345678");
        usuarioAnterior.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        usuarioAnterior.setSexo(Sexo.MASCULINO);
        usuarioAnterior.setDireccion("Calle Falsa 123");
        usuarioAnterior.setEmail("juan.perez@example.com");
        usuarioAnterior.setTelefono("1122334455");
        usuarioAnterior.setPassword("password123");
        usuarioAnterior.setRol(Rol.ADMIN);
        usuarioAnterior.setActivo(true);
        usuarioAnterior.setFechaCreacion(LocalDateTime.now());

        usuarioNuevo = new Usuario();
        usuarioNuevo.setId(1L);
        usuarioNuevo.setNombre("Juan Carlos");
        usuarioNuevo.setApellido("Pérez");
        usuarioNuevo.setDni("12345678");
        usuarioNuevo.setFechaNacimiento(LocalDate.of(1980, 1, 1));
        usuarioNuevo.setSexo(Sexo.MASCULINO);
        usuarioNuevo.setDireccion("Calle Nueva 456");
        usuarioNuevo.setEmail("juancarlos.perez@example.com");
        usuarioNuevo.setTelefono("1122334455");
        usuarioNuevo.setPassword("password123");
        usuarioNuevo.setRol(Rol.ADMIN);
        usuarioNuevo.setActivo(true);
        usuarioNuevo.setFechaCreacion(LocalDateTime.now());

        usuarioModificador = new Usuario();
        usuarioModificador.setId(2L);
        usuarioModificador.setNombre("Admin");
        usuarioModificador.setApellido("Sistema");
        usuarioModificador.setDni("87654321");
        usuarioModificador.setRol(Rol.SUPERUSER);
    }

    @Test
    void auditarCambioUsuario_DebeAuditarCambiosEnCampos() {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(auditoriaRepository.save(any(AuditoriaUsuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        auditoriaService.auditarCambioUsuario(usuarioAnterior, usuarioNuevo, usuarioModificador, TipoOperacion.MODIFICACION);

        // Then
        // Verificar que se guardaron registros para los campos que cambiaron
        verify(auditoriaRepository, times(3)).save(any(AuditoriaUsuario.class));
    }

    @Test
    void auditarOperacion_DebeAuditarOperacion() {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(auditoriaRepository.save(any(AuditoriaUsuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        auditoriaService.auditarOperacion(usuarioAnterior, usuarioModificador, TipoOperacion.CAMBIO_PASSWORD, "Cambio de contraseña");

        // Then
        verify(auditoriaRepository).save(any(AuditoriaUsuario.class));
    }

    @Test
    void obtenerAuditoriaUsuario_DebeRetornarRegistrosDeAuditoria() {
        // Given
        AuditoriaUsuario auditoria1 = new AuditoriaUsuario();
        auditoria1.setId(1L);
        auditoria1.setUsuarioModificadoId(1L);
        auditoria1.setUsuarioModificadoDni("12345678");
        auditoria1.setUsuarioQueModificaId(2L);
        auditoria1.setUsuarioQueModificaDni("87654321");
        auditoria1.setCampoModificado("nombre");
        auditoria1.setValorAnterior("Juan");
        auditoria1.setValorNuevo("Juan Carlos");
        auditoria1.setTipoOperacion(TipoOperacion.MODIFICACION);
        auditoria1.setFechaModificacion(LocalDateTime.now());

        AuditoriaUsuario auditoria2 = new AuditoriaUsuario();
        auditoria2.setId(2L);
        auditoria2.setUsuarioModificadoId(1L);
        auditoria2.setUsuarioModificadoDni("12345678");
        auditoria2.setUsuarioQueModificaId(2L);
        auditoria2.setUsuarioQueModificaDni("87654321");
        auditoria2.setCampoModificado("direccion");
        auditoria2.setValorAnterior("Calle Falsa 123");
        auditoria2.setValorNuevo("Calle Nueva 456");
        auditoria2.setTipoOperacion(TipoOperacion.MODIFICACION);
        auditoria2.setFechaModificacion(LocalDateTime.now());

        when(auditoriaRepository.findByUsuarioModificadoIdOrderByFechaModificacionDesc(1L))
                .thenReturn(Arrays.asList(auditoria1, auditoria2));

        // When
        List<AuditoriaUsuario> result = auditoriaService.obtenerAuditoriaUsuario(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(auditoriaRepository).findByUsuarioModificadoIdOrderByFechaModificacionDesc(1L);
    }

    @Test
    void obtenerAuditoriaPorDni_DebeRetornarRegistrosDeAuditoria() {
        // Given
        AuditoriaUsuario auditoria = new AuditoriaUsuario();
        auditoria.setId(1L);
        auditoria.setUsuarioModificadoId(1L);
        auditoria.setUsuarioModificadoDni("12345678");
        auditoria.setUsuarioQueModificaId(2L);
        auditoria.setUsuarioQueModificaDni("87654321");
        auditoria.setCampoModificado("nombre");
        auditoria.setValorAnterior("Juan");
        auditoria.setValorNuevo("Juan Carlos");
        auditoria.setTipoOperacion(TipoOperacion.MODIFICACION);
        auditoria.setFechaModificacion(LocalDateTime.now());

        when(auditoriaRepository.findByUsuarioModificadoDni("12345678"))
                .thenReturn(Arrays.asList(auditoria));

        // When
        List<AuditoriaUsuario> result = auditoriaService.obtenerAuditoriaPorDni("12345678");

        // Then
        assertThat(result).hasSize(1);
        verify(auditoriaRepository).findByUsuarioModificadoDni("12345678");
    }

    @Test
    void obtenerTodasLasAuditorias_DebeRetornarTodasLasAuditorias() {
        // Given
        AuditoriaUsuario auditoria1 = new AuditoriaUsuario();
        auditoria1.setId(1L);
        AuditoriaUsuario auditoria2 = new AuditoriaUsuario();
        auditoria2.setId(2L);

        when(auditoriaRepository.findAll()).thenReturn(Arrays.asList(auditoria1, auditoria2));

        // When
        List<AuditoriaUsuario> result = auditoriaService.obtenerTodasLasAuditorias();

        // Then
        assertThat(result).hasSize(2);
        verify(auditoriaRepository).findAll();
    }
}
