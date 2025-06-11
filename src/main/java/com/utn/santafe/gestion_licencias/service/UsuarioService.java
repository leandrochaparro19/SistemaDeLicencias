package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaService auditoriaService;

    public Usuario guardarUsuario(Usuario usuario) {
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado, Usuario usuarioQueModifica) {
        var usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear copia del usuario anterior para auditoría
        Usuario usuarioAnterior = clonarUsuario(usuarioExistente);

        // Actualizar campos (excepto password y rol) - con validaciones null
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getApellido() != null) {
            usuarioExistente.setApellido(usuarioActualizado.getApellido());
        }
        if (usuarioActualizado.getDni() != null) {
            usuarioExistente.setDni(usuarioActualizado.getDni());
        }
        if (usuarioActualizado.getFechaNacimiento() != null) {
            usuarioExistente.setFechaNacimiento(usuarioActualizado.getFechaNacimiento());
        }
        if (usuarioActualizado.getSexo() != null) {
            usuarioExistente.setSexo(usuarioActualizado.getSexo());
        }
        if (usuarioActualizado.getDireccion() != null) {
            usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
        }
        if (usuarioActualizado.getEmail() != null) {
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }
        // El teléfono puede ser null, así que lo actualizamos siempre
        usuarioExistente.setTelefono(usuarioActualizado.getTelefono());

        usuarioExistente.setFechaUltimaModificacion(LocalDateTime.now());
        usuarioExistente.setUsuarioModificadorId(usuarioQueModifica.getId());

        Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);

        // Auditar cambios
        auditoriaService.auditarCambioUsuario(usuarioAnterior, usuarioGuardado, usuarioQueModifica, TipoOperacion.MODIFICACION);

        return usuarioGuardado;
    }

    // Método sobrecargado para compatibilidad con el controlador
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        var usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar solo los campos editables
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellido(usuarioActualizado.getApellido());
        usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setTelefono(usuarioActualizado.getTelefono());

        usuarioExistente.setFechaUltimaModificacion(LocalDateTime.now());

        return usuarioRepository.save(usuarioExistente);
    }

    public void cambiarPassword(Long id, String nuevaPassword, Usuario usuarioQueModifica) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
        usuario.setUsuarioModificadorId(usuarioQueModifica.getId());
        usuarioRepository.save(usuario);

        // Auditar cambio de contraseña
        auditoriaService.auditarOperacion(usuario, usuarioQueModifica, TipoOperacion.CAMBIO_PASSWORD, "Cambio de contraseña");
    }

    public void toggleEstadoUsuario(Long id, Usuario usuarioQueModifica) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Usuario usuarioAnterior = clonarUsuario(usuario);
        usuario.setActivo(!usuario.isActivo());
        usuario.setFechaUltimaModificacion(LocalDateTime.now());
        usuario.setUsuarioModificadorId(usuarioQueModifica.getId());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Auditar cambio de estado
        auditoriaService.auditarCambioUsuario(usuarioAnterior, usuarioGuardado, usuarioQueModifica,
                usuario.isActivo() ? TipoOperacion.ACTIVACION : TipoOperacion.DESACTIVACION);
    }

    private Usuario clonarUsuario(Usuario usuario) {
        Usuario clon = new Usuario();
        clon.setId(usuario.getId());
        clon.setNombre(usuario.getNombre());
        clon.setApellido(usuario.getApellido());
        clon.setDni(usuario.getDni());
        clon.setFechaNacimiento(usuario.getFechaNacimiento());
        clon.setSexo(usuario.getSexo());
        clon.setDireccion(usuario.getDireccion());
        clon.setEmail(usuario.getEmail());
        clon.setTelefono(usuario.getTelefono());
        clon.setPassword(usuario.getPassword());
        clon.setRol(usuario.getRol());
        clon.setActivo(usuario.isActivo());
        clon.setFechaCreacion(usuario.getFechaCreacion());
        clon.setFechaUltimaModificacion(usuario.getFechaUltimaModificacion());
        clon.setUsuarioCreadorId(usuario.getUsuarioCreadorId());
        clon.setUsuarioModificadorId(usuario.getUsuarioModificadorId());
        return clon;
    }

    // Métodos existentes actualizados para incluir auditoría
    public Optional<Usuario> buscarPorDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public List<Usuario> listarInactivos() {
        return usuarioRepository.findByActivoFalse();
    }

    public boolean existeDni(String dni) {
        return usuarioRepository.existsByDni(dni);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public boolean existeDniParaOtroUsuario(String dni, Long idUsuario) {
        return usuarioRepository.existsByDniAndIdNot(dni, idUsuario);
    }

    public boolean existeEmailParaOtroUsuario(String email, Long idUsuario) {
        return usuarioRepository.existsByEmailAndIdNot(email, idUsuario);
    }

    public void eliminarUsuario(Long id, Usuario usuarioQueModifica) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Auditar eliminación antes de eliminar
        auditoriaService.auditarOperacion(usuario, usuarioQueModifica, TipoOperacion.ELIMINACION, "Usuario eliminado");

        usuarioRepository.deleteById(id);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    public long contarUsuariosInactivos() {
        return usuarioRepository.countByActivoFalse();
    }
}
