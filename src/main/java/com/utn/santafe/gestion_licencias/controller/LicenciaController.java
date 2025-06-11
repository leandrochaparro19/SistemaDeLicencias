package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.service.LicenciaService;
import com.utn.santafe.gestion_licencias.model.form.LicenciaForm;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;

import com.utn.santafe.gestion_licencias.service.TitularService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/licencias")
public class LicenciaController {

    @Autowired
    private TitularRepository titularRepository;

    @Autowired
    private TitularService titularService;

    @Autowired
    private LicenciaService licenciaService;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @GetMapping("/nueva/{titularId}")
    public String nuevaLicencia(@PathVariable Long titularId, Model model) {

        Titular t = titularRepository.findById(titularId).orElseThrow(() -> new EntityNotFoundException("El titular no existe"));
        LicenciaForm form = new LicenciaForm();
        form.setTitularId(titularId);

        model.addAttribute("titular", t);
        model.addAttribute("clases", ClaseLicencia.values());
        model.addAttribute("licenciaForm", form);
        return "licencias/form";
    }

    @PostMapping("/guardar")
    public String guardarLicencia(@Valid @ModelAttribute("licenciaForm") LicenciaForm form,
                                  BindingResult br,
                                  Model model,
                                  RedirectAttributes ra,
                                  @AuthenticationPrincipal UserDetails currentUser) { // Inyectar usuario directamente

        if (br.hasErrors()) {
            model.addAttribute("clases", ClaseLicencia.values());
            Titular t = titularRepository.findById(form.getTitularId())
                    .orElseThrow(() -> new EntityNotFoundException("Titular no existe"));
            model.addAttribute("titular", t);
            return "licencias/form";
        }

        try {
            // Verificar que haya usuario autenticado
            if (currentUser == null) {
                ra.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }

            Licencia nueva = licenciaService.emitirLicencia(
                    form.getTitularId(),
                    form.getClase(),
                    form.getObservaciones(),
                    currentUser.getUsername()  // El username es el DNI en tu configuración
            );

            ra.addFlashAttribute("success", "Licencia emitida ID " + nueva.getId());
        } catch(IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/titulares/lista";
    }

    @GetMapping("/lista")
    public String verListadoLicencias(Model model) {
        model.addAttribute("licencias", licenciaService.listarLicencias());
        return "licencias/lista";
    }

    @GetMapping("/{id}/imprimirLicencia")
    public String imprimirLicencia(@PathVariable Long id, Model model) {
        Licencia licencia = licenciaService.buscarPorId(id);
        model.addAttribute("licencia", licencia);
        return "licencias/imprimirLicencia";
    }

    @GetMapping("/{id}/renovar")
    public String mostrarFormularioRenovar(@PathVariable Long id, Model model) {
        Licencia licenciaActual = licenciaService.buscarPorId(id);
        if (licenciaActual == null) {
            // si no existe, redirigir a list o mostrar error
            return "redirect:/licencias?error=LicenciaNoEncontrada";
        }

        LicenciaForm form = new LicenciaForm();
        form.setTitularId(licenciaActual.getTitular().getId());
        form.setClase(licenciaActual.getClase());  // precargar clase actual
        form.setObservaciones(licenciaActual.getObservaciones());

        model.addAttribute("licenciaActual", licenciaActual);
        model.addAttribute("licenciaForm", form);
        model.addAttribute("clases", ClaseLicencia.values());
        return "licencias/formRenovar";
    }

    @PostMapping("/{id}/renovar")
    public String procesarRenovacion(
            @PathVariable Long id,
            @Valid @ModelAttribute("licenciaForm") LicenciaForm form,
            BindingResult br,
            RedirectAttributes ra,
            Model model,
            @AuthenticationPrincipal UserDetails currentUser) { // Inyectar usuario directamente

        Licencia licenciaActual = licenciaService.buscarPorId(id);
        if (licenciaActual == null) {
            return "redirect:/licencias?error=LicenciaNoEncontrada";
        }

        if (br.hasErrors()) {
            model.addAttribute("licenciaActual", licenciaActual);
            model.addAttribute("clases", ClaseLicencia.values());
            return "licencias/formRenovar";
        }

        // Verificar que haya usuario autenticado
        if (currentUser == null) {
            ra.addFlashAttribute("error", "Usuario no autenticado");
            return "redirect:/login";
        }

        Licencia renovada = licenciaService.renovarLicencia(
                licenciaActual.getId(),
                form.getClase(),
                form.getObservaciones(),
                currentUser.getUsername()  // El username es el DNI
        );

        ra.addFlashAttribute("success", "Licencia renovada (ID: " + renovada.getId() + ")");
        return "redirect:/licencias/" + renovada.getId() + "/imprimirLicencia";
    }
}
