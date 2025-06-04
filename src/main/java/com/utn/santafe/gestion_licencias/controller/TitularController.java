package com.utn.santafe.gestion_licencias.controller;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.service.LicenciaService;
import com.utn.santafe.gestion_licencias.service.TitularService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.utn.santafe.gestion_licencias.model.titular.TipoDocumento;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/titulares")
public class TitularController {

    @Autowired
    private TitularService titularService;

    @Autowired
    private LicenciaService licenciaService;

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute("titular", new Titular());
        model.addAttribute("tiposDoc",       TipoDocumento.values());
        model.addAttribute("clasesLicencia", ClaseLicencia.values());
        model.addAttribute("grupos",         GrupoSanguineo.values());
        model.addAttribute("factores",       FactorRh.values());
        return "titulares/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Titular titular, BindingResult br, Model model){
        if (br.hasErrors()){
            rellenarCombos(model);
            return "titulares/formulario";
        }
        titularService.guardarTitular(titular);
        return "redirect:/titulares/lista";
    }

    @GetMapping("/lista")
    public String listarTitulares(Model model
    ) {
        model.addAttribute("titulares", titularService.listarTitulares());
        return "titulares/lista";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model){
        Titular t = titularService.buscarPorId(id);
        if (t == null) return "redirect:/titulares/lista";
        rellenarCombos(model);
        model.addAttribute("titular", t);
        return "titulares/formulario";  // reutiliza la misma vista
    }

    /* ---------- ELIMINAR (confirmación) ---------- */
    @GetMapping("/eliminar/{id}")
    public String confirmarEliminar(@PathVariable Long id, Model m){
        Titular t = titularService.buscarPorId(id);
        if(t == null) return "redirect:/titulares/lista";
        m.addAttribute("titular", t);
        return "titulares/confirmar-eliminar";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id){
        titularService.eliminarPorId(id);
        return "redirect:/titulares/lista";
    }

    private void rellenarCombos(Model m){
        m.addAttribute("tiposDoc",       TipoDocumento.values());
        m.addAttribute("clasesLicencia", ClaseLicencia.values());
        m.addAttribute("grupos",         GrupoSanguineo.values());
        m.addAttribute("factores",       FactorRh.values());
    }

    @GetMapping("/titular/{titularId}/historial")
    public String listarHistorialPorTitular(@PathVariable Long titularId, Model model) {
        List<Licencia> historial = licenciaService.listarHistorialPorTitular(titularId);
        model.addAttribute("historialLicencias", historial);
        return "Titulares/historialTitular";
    }
}