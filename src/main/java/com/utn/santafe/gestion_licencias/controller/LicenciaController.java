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

import java.time.LocalDate;

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
                                  RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("clases", ClaseLicencia.values());
            Titular t = titularRepository.findById(form.getTitularId())
                    .orElseThrow(() -> new EntityNotFoundException("Titular no existe"));
            model.addAttribute("titular", t);
            return "licencias/form";
        }

        Licencia nueva = licenciaService.emitirLicencia(
                form.getTitularId(),
                form.getClase(),
                form.getObservaciones(),
                "admin"                         // Remplazarlo por el usuario real cuando implemente spring security
        );

        ra.addFlashAttribute("success", "Licencia emitida ID " + nueva.getId());
        return "redirect:/titulares/lista";
    }
}
