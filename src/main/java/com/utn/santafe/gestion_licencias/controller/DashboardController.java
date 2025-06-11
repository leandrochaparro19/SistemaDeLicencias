package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String mostrarDashboard(Authentication authentication, Model model) {
        String dni = authentication.getName();
        var usuario = usuarioService.buscarPorDni(dni);

        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            model.addAttribute("totalUsuarios", usuarioService.contarUsuarios());
            model.addAttribute("usuariosActivos", usuarioService.contarUsuariosActivos());
            model.addAttribute("usuariosInactivos", usuarioService.contarUsuariosInactivos());
        }

        return "dashboard/index";
    }
}
