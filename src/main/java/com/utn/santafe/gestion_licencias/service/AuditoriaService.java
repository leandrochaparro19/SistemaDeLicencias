package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.auditoria.AuditoriaUsuario;
import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.repository.AuditoriaUsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditoriaService {

    @Autowired
    private AuditoriaUsuarioRepository auditoriaRepository;

    @Autowired
    private HttpServletRequest request;

    public void auditarCambioUsuario(Usuario usuarioAnterior, Usuario usuarioNuevo,
                                     Usuario usuarioQueModifica, TipoOperacion tipoOperacion) {

        String ipOrigen = obtenerIpCliente();

        // Auditar cambios en campos específicos con validaciones null
        if (usuarioAnterior.getNombre() != null && usuarioNuevo.getNombre() != null &&
                !usuarioAnterior.getNombre().equals(usuarioNuevo.getNombre())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "nombre", usuarioAnterior.getNombre(), usuarioNuevo.getNombre(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getApellido() != null && usuarioNuevo.getApellido() != null &&
                !usuarioAnterior.getApellido().equals(usuarioNuevo.getApellido())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "apellido", usuarioAnterior.getApellido(), usuarioNuevo.getApellido(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getDni() != null && usuarioNuevo.getDni() != null &&
                !usuarioAnterior.getDni().equals(usuarioNuevo.getDni())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "dni", usuarioAnterior.getDni(), usuarioNuevo.getDni(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getDireccion() != null && usuarioNuevo.getDireccion() != null &&
                !usuarioAnterior.getDireccion().equals(usuarioNuevo.getDireccion())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "direccion", usuarioAnterior.getDireccion(), usuarioNuevo.getDireccion(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getEmail() != null && usuarioNuevo.getEmail() != null &&
                !usuarioAnterior.getEmail().equals(usuarioNuevo.getEmail())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "email", usuarioAnterior.getEmail(), usuarioNuevo.getEmail(),
                    tipoOperacion, ipOrigen);
        }

        // Auditar teléfono (puede ser null)
        String telefonoAnterior = usuarioAnterior.getTelefono() != null ? usuarioAnterior.getTelefono() : "";
        String telefonoNuevo = usuarioNuevo.getTelefono() != null ? usuarioNuevo.getTelefono() : "";
        if (!telefonoAnterior.equals(telefonoNuevo)) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "telefono", telefonoAnterior, telefonoNuevo,
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getFechaNacimiento() != null && usuarioNuevo.getFechaNacimiento() != null &&
                !usuarioAnterior.getFechaNacimiento().equals(usuarioNuevo.getFechaNacimiento())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "fechaNacimiento", usuarioAnterior.getFechaNacimiento().toString(),
                    usuarioNuevo.getFechaNacimiento().toString(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.getSexo() != null && usuarioNuevo.getSexo() != null &&
                !usuarioAnterior.getSexo().equals(usuarioNuevo.getSexo())) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "sexo", usuarioAnterior.getSexo().getDescripcion(),
                    usuarioNuevo.getSexo().getDescripcion(),
                    tipoOperacion, ipOrigen);
        }

        if (usuarioAnterior.isActivo() != usuarioNuevo.isActivo()) {
            crearRegistroAuditoria(usuarioAnterior, usuarioNuevo, usuarioQueModifica,
                    "activo", String.valueOf(usuarioAnterior.isActivo()),
                    String.valueOf(usuarioNuevo.isActivo()),
                    usuarioNuevo.isActivo() ? TipoOperacion.ACTIVACION : TipoOperacion.DESACTIVACION,
                    ipOrigen);
        }
    }

    public void auditarOperacion(Usuario usuario, Usuario usuarioQueModifica,
                                 TipoOperacion tipoOperacion, String detalle) {
        String ipOrigen = obtenerIpCliente();

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                usuario.getId(),
                usuario.getDni(),
                usuarioQueModifica.getId(),
                usuarioQueModifica.getDni(),
                tipoOperacion.name(),
                "",
                detalle,
                tipoOperacion,
                ipOrigen
        );

        auditoriaRepository.save(auditoria);
    }

    private void crearRegistroAuditoria(Usuario usuarioAnterior, Usuario usuarioNuevo,
                                        Usuario usuarioQueModifica, String campo,
                                        String valorAnterior, String valorNuevo,
                                        TipoOperacion tipoOperacion, String ipOrigen) {

        AuditoriaUsuario auditoria = new AuditoriaUsuario(
                usuarioAnterior.getId(),
                usuarioAnterior.getDni(),
                usuarioQueModifica.getId(),
                usuarioQueModifica.getDni(),
                campo,
                valorAnterior,
                valorNuevo,
                tipoOperacion,
                ipOrigen
        );

        auditoriaRepository.save(auditoria);
    }

    private String obtenerIpCliente() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    public List<AuditoriaUsuario> obtenerAuditoriaUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioModificadoIdOrderByFechaModificacionDesc(usuarioId);
    }

    public List<AuditoriaUsuario> obtenerAuditoriaPorDni(String dni) {
        return auditoriaRepository.findByUsuarioModificadoDni(dni);
    }

    public List<AuditoriaUsuario> obtenerTodasLasAuditorias() {
        return auditoriaRepository.findAll();
    }
}
