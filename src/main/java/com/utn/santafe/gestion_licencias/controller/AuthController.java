package com.utn.santafe.gestion_licencias.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {

        if (error != null) {
            model.addAttribute("error", "DNI o contraseña incorrectos");
        }

        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }

        return "auth/login";
    }
}
