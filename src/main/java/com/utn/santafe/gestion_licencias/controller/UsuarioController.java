package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.service.AuditoriaService;
import com.utn.santafe.gestion_licencias.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('SUPERUSER') or hasRole('ADMIN')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping
    public String listarUsuarios(@RequestParam(value = "estado", required = false) String estado, Model model) {
        var usuarios = usuarioService.listarTodos();

        // Filtrar por estado si se especifica
        if ("activos".equals(estado)) {
            usuarios = usuarios.stream().filter(Usuario::isActivo).toList();
        } else if ("inactivos".equals(estado)) {
            usuarios = usuarios.stream().filter(u -> !u.isActivo()).toList();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("estadoFiltro", estado);
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('SUPERUSER')")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("sexos", Sexo.values());
        return "usuarios/crear-usuario";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('SUPERUSER')")
    public String guardarUsuario(@Valid @ModelAttribute Usuario usuario,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Validaciones adicionales
        if (usuarioService.existeDni(usuario.getDni())) {
            result.rejectValue("dni", "error.usuario", "Ya existe un usuario con este DNI");
        }

        if (usuarioService.existeEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "Ya existe un usuario con este email");
        }

        if (result.hasErrors()) {
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("editando", false);
            return "usuarios/crear-usuario";
        }

        try {
            // Asignar rol ADMIN automáticamente
            usuario.setRol(Rol.ADMIN);
            usuarioService.guardarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios/nuevo";
        }
    }

    @GetMapping("/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            // Obtener historial de auditoría
            var auditoria = auditoriaService.obtenerAuditoriaUsuario(id);
            model.addAttribute("historialAuditoria", auditoria);
            return "usuarios/detalle";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/editar-usuario";
        }
        redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizarUsuario(@PathVariable Long id,
                                    @Valid @ModelAttribute Usuario usuario,
                                    BindingResult result,
                                    Model model,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {

        var usuarioExistente = usuarioService.buscarPorId(id);

        if (usuarioExistente.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios";
        }

        // Validaciones adicionales
        if (usuarioService.existeDniParaOtroUsuario(usuario.getDni(), id)) {
            result.rejectValue("dni", "error.usuario", "Ya existe otro usuario con este DNI");
        }

        if (usuarioService.existeEmailParaOtroUsuario(usuario.getEmail(), id)) {
            result.rejectValue("email", "error.usuario", "Ya existe otro usuario con este email");
        }

        if (result.hasErrors()) {
            model.addAttribute("sexos", Sexo.values());
            model.addAttribute("editando", true);
            return "usuarios/editar-usuario";
        }

        try {
            // Obtener usuario que modifica
            String dni = authentication.getName();
            var usuarioQueModifica = usuarioService.buscarPorDni(dni).orElseThrow();

            usuarioService.actualizarUsuario(id, usuario, usuarioQueModifica);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/usuarios/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios/" + id + "/editar";
        }
    }

    @GetMapping("/{id}/cambiar-password")
    public String mostrarFormularioCambiarPassword(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "usuarios/cambiar-password";
        }
        redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
        redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/cambiar-password")
    public String cambiarPassword(@PathVariable Long id,
                                  @RequestParam String nuevaPassword,
                                  @RequestParam String confirmarPassword,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("mensaje", "La contraseña debe tener al menos 8 caracteres");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios/" + id + "/cambiar-password";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("mensaje", "Las contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios/" + id + "/cambiar-password";
        }

        try {
            // Obtener usuario que modifica
            String dni = authentication.getName();
            var usuarioQueModifica = usuarioService.buscarPorDni(dni).orElseThrow();

            usuarioService.cambiarPassword(id, nuevaPassword, usuarioQueModifica);
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/usuarios/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar la contraseña: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios/" + id + "/cambiar-password";
        }
    }

    @PostMapping("/{id}/toggle-estado")
    public String toggleEstadoUsuario(@PathVariable Long id,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {

        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios";
        }

        // No permitir desactivar al propio superusuario
        String dniActual = authentication.getName();
        if (usuario.get().getDni().equals(dniActual)) {
            redirectAttributes.addFlashAttribute("mensaje", "No puedes desactivar tu propia cuenta");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/usuarios";
        }

        try {
            // Obtener usuario que modifica
            var usuarioQueModifica = usuarioService.buscarPorDni(dniActual).orElseThrow();

            usuarioService.toggleEstadoUsuario(id, usuarioQueModifica);
            String estado = usuario.get().isActivo() ? "desactivado" : "activado";
            redirectAttributes.addFlashAttribute("mensaje", "Usuario " + estado + " exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar el estado del usuario");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize("hasRole('SUPERUSER')")
    public String eliminarUsuario(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios";
        }

        // No permitir eliminar al propio superusuario
        String dniActual = authentication.getName();
        if (usuario.get().getDni().equals(dniActual)) {
            redirectAttributes.addFlashAttribute("mensaje", "No puedes eliminar tu propia cuenta");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/usuarios";
        }

        try {
            // Obtener usuario que modifica
            var usuarioQueModifica = usuarioService.buscarPorDni(dniActual).orElseThrow();

            usuarioService.eliminarUsuario(id, usuarioQueModifica);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el usuario");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/auditoria")
    public String verAuditoriaUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.buscarPorId(id);
        if (usuario.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/usuarios";
        }

        var auditoria = auditoriaService.obtenerAuditoriaUsuario(id);
        model.addAttribute("usuario", usuario.get());
        model.addAttribute("historialAuditoria", auditoria);

        return "usuarios/auditoria";
    }
}
