package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class LicenciaService {

    @Autowired
    private LicenciaRepository licenciaRepository;
    @Autowired private TitularRepository titularRepository;

    public Licencia emitirLicencia(Long titularId, ClaseLicencia clase, String obs, String usuario) {

        Titular t = titularRepository.findById(titularId)
                .orElseThrow(EntityNotFoundException::new);
        validarEdadMinima(t, clase);
        validarProfesional(t, clase);

        boolean primeraVez = licenciaRepository.countByTitularAndClaseAndVigente(t, clase, true) == 0;
        LocalDate hoy   = LocalDate.now();
        LocalDate vence = calcularVencimiento(t, clase, hoy, primeraVez);

        Licencia lic = new Licencia();
        lic.setTitular(t);
        lic.setClase(clase);
        lic.setFechaEmision(hoy);
        lic.setFechaVencimiento(vence);
        lic.setObservaciones(obs);
        lic.setUsuarioAdmin(usuario);
        lic.setVigente(true);


        //licenciaRepository.findByTitularAndClaseAndVigente(t, clase, true).ifPresent(l -> { l.setVigente(false); licenciaRepository.save(l); });    //Solo setea la ultima licencia vigente a false

        // setea todas las licencias vigentes anteriores a false
        List<Licencia> vigentesPrevias = licenciaRepository.findByTitularAndVigente(t, true);
        for (Licencia prev : vigentesPrevias) {
            prev.setVigente(false);
            licenciaRepository.save(prev);
        }

        return licenciaRepository.save(lic);
    }

    private void validarEdadMinima(Titular t, ClaseLicencia clase) {
        int edad = Period.between(t.getFechaNacimiento(), LocalDate.now()).getYears();
        int minimo = Set.of(ClaseLicencia.C, ClaseLicencia.D, ClaseLicencia.E).contains(clase) ? 21 : 17;
        if (edad < minimo)
            throw new IllegalArgumentException("El titular no cumple la edad mínima");
    }

    private void validarProfesional(Titular t, ClaseLicencia clase) {
        if (!Set.of(ClaseLicencia.C, ClaseLicencia.D, ClaseLicencia.E).contains(clase)) return;
        boolean tieneB1 =
                licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                        t, ClaseLicencia.B, LocalDate.now().minusYears(1));
        if (!tieneB1)
            throw new IllegalArgumentException("Debe poseer clase B con al menos un año de antigüedad");
    }

    public LocalDate calcularVencimiento(Titular t, ClaseLicencia c, LocalDate fechaEmision, boolean esPrimeraVez) {

        int edad = Period.between(t.getFechaNacimiento(), fechaEmision).getYears();
        int anios;

        if (edad < 21) anios = esPrimeraVez ? 1 : 3;
        else if (edad <= 46) anios = 5;
        else if (edad <= 60) anios = 4;
        else if (edad <= 70) anios = 3;
        else anios = 1;

        LocalDate venc = fechaEmision.plusYears(anios)
                .withMonth(t.getFechaNacimiento().getMonthValue())
                .withDayOfMonth(t.getFechaNacimiento().getDayOfMonth());

        /* si el día/mes ya pasaron este año, pasa al siguiente */
        if (!venc.isAfter(fechaEmision)) venc = venc.plusYears(1);

        return venc;
    }
}
