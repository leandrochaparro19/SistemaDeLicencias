package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.usuario.Rol;
import com.utn.santafe.gestion_licencias.model.usuario.Sexo;
import com.utn.santafe.gestion_licencias.model.usuario.Usuario;
import com.utn.santafe.gestion_licencias.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String verPerfil(Authentication authentication, Model model) {
        String dni = authentication.getName();
        var usuario = usuarioService.buscarPorDni(dni);

        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "perfil/mi-perfil";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/editar")
    public String mostrarFormularioEditar(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        String dni = authentication.getName();
        var usuario = usuarioService.buscarPorDni(dni);

        if (usuario.isEmpty()) {
            return "redirect:/perfil";
        }

        // Verificar que no sea ADMIN
        if (usuario.get().getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("mensaje", "Los administradores no pueden editar su perfil");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario.get());
        model.addAttribute("sexos", Sexo.values());
        return "perfil/editar-perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@Valid @ModelAttribute Usuario usuario,
                                   BindingResult result,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        String dni = authentication.getName();
        var usuarioActual = usuarioService.buscarPorDni(dni);

        if (usuarioActual.isEmpty()) {
            return "redirect:/perfil";
        }

        // Verificar que no sea ADMIN
        if (usuarioActual.get().getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("mensaje", "Los administradores no pueden editar su perfil");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/perfil";
        }

        Long idActual = usuarioActual.get().getId();

        // Validaciones adicionales - solo para campos editables
        if (!usuario.getEmail().equals(usuarioActual.get().getEmail()) &&
                usuarioService.existeEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "Ya existe un usuario con este email");
        }

        if (result.hasErrors()) {
            model.addAttribute("sexos", Sexo.values());
            return "perfil/editar-perfil";
        }

        try {
            // Solo actualizar campos permitidos
            Usuario usuarioParaActualizar = new Usuario();
            usuarioParaActualizar.setNombre(usuario.getNombre());
            usuarioParaActualizar.setApellido(usuario.getApellido());
            usuarioParaActualizar.setDireccion(usuario.getDireccion());
            usuarioParaActualizar.setEmail(usuario.getEmail());
            usuarioParaActualizar.setTelefono(usuario.getTelefono());

            usuarioService.actualizarUsuario(idActual, usuarioParaActualizar, usuarioActual.get());
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil/editar";
        }
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambiarPassword(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        String dni = authentication.getName();
        var usuario = usuarioService.buscarPorDni(dni);

        if (usuario.isEmpty()) {
            return "redirect:/perfil";
        }

        // Verificar que no sea ADMIN
        if (usuario.get().getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("mensaje", "Los administradores no pueden cambiar su contraseña");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/perfil";
        }

        model.addAttribute("usuario", usuario.get());
        return "perfil/cambiar-mi-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@RequestParam String passwordActual,
                                  @RequestParam String nuevaPassword,
                                  @RequestParam String confirmarPassword,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {

        String dni = authentication.getName();
        var usuario = usuarioService.buscarPorDni(dni);

        if (usuario.isEmpty()) {
            return "redirect:/perfil";
        }

        // Verificar que no sea ADMIN
        if (usuario.get().getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("mensaje", "Los administradores no pueden cambiar su contraseña");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/perfil";
        }

        // Validar contraseña actual
        if (!passwordEncoder.matches(passwordActual, usuario.get().getPassword())) {
            redirectAttributes.addFlashAttribute("mensaje", "La contraseña actual es incorrecta");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil/cambiar-password";
        }

        // Validar nueva contraseña
        if (nuevaPassword == null || nuevaPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("mensaje", "La nueva contraseña debe tener al menos 8 caracteres");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil/cambiar-password";
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("mensaje", "Las contraseñas no coinciden");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil/cambiar-password";
        }

        try {
            usuarioService.cambiarPassword(usuario.get().getId(), nuevaPassword, usuario.get());
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar la contraseña: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/perfil/cambiar-password";
        }
    }
}
