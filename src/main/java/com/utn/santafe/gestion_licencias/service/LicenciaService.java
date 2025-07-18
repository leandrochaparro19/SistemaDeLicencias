package com.utn.santafe.gestion_licencias.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.licencia.tipoEmision;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class LicenciaService {

    @Autowired
    private LicenciaRepository licenciaRepository;
    @Autowired
    private TitularRepository titularRepository;

    public Licencia emitirLicencia(Long titularId, ClaseLicencia clase, String obs, String usuario) {

        Titular t = titularRepository.findById(titularId)
                .orElseThrow(EntityNotFoundException::new);
        validarEdadMinima(t, clase);
        validarProfesional(t, clase);

        boolean primeraVez = licenciaRepository.countByTitularAndClaseAndVigente(t, clase, true) == 0;
        LocalDate hoy = LocalDate.now();
        LocalDate vence = calcularVencimiento(t, clase, hoy, primeraVez);

        int aniosVigencia = Period.between(hoy, vence).getYears();
        BigDecimal costo = calcularCostoLicencia(clase, aniosVigencia);

        Licencia lic = new Licencia();
        lic.setTitular(t);
        lic.setClase(clase);
        lic.setFechaEmision(hoy);
        lic.setFechaVencimiento(vence);
        lic.setObservaciones(obs);
        lic.setUsuarioAdmin(usuario);
        lic.setVigente(true);
        lic.setTipoEmision(tipoEmision.ORIGINAL);
        lic.setCosto(costo);

        // licenciaRepository.findByTitularAndClaseAndVigente(t, clase,
        // true).ifPresent(l -> { l.setVigente(false); licenciaRepository.save(l); });
        // //Solo setea la ultima licencia vigente a false

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
        if (!Set.of(ClaseLicencia.C, ClaseLicencia.D, ClaseLicencia.E).contains(clase))
            return;
        boolean tieneB1 = licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                t, ClaseLicencia.B, LocalDate.now().minusYears(1));
        if (!tieneB1)
            throw new IllegalArgumentException("Debe poseer clase B con al menos un año de antiguedad");
    }

    public LocalDate calcularVencimiento(Titular t, ClaseLicencia c, LocalDate fechaEmision, boolean esPrimeraVez) {

        int edad = Period.between(t.getFechaNacimiento(), fechaEmision).getYears();
        int anios;

        if (edad < 21)
            anios = esPrimeraVez ? 1 : 3;
        else if (edad <= 46)
            anios = 5;
        else if (edad <= 60)
            anios = 4;
        else if (edad <= 70)
            anios = 3;
        else
            anios = 1;

        LocalDate venc = fechaEmision.plusYears(anios)
                .withMonth(t.getFechaNacimiento().getMonthValue())
                .withDayOfMonth(t.getFechaNacimiento().getDayOfMonth());

        /* si el día/mes ya pasaron este año, pasa al siguiente */
        if (!venc.isAfter(fechaEmision))
            venc = venc.plusYears(1);

        return venc;
    }

    public List<Licencia> listarLicencias() {
        return licenciaRepository.findAll();
    }

    public Licencia buscarPorId(Long id) {
        return licenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Licencia no encontrada con ID " + id));
    }

    public Licencia renovarLicencia(Long licenciaAnteriorId, ClaseLicencia nuevaClase, String obs,
                                    String usuarioAdmin) {

        // 1. Obtener la licencia anterior y el titular asociado
        Licencia anterior = licenciaRepository.findById(licenciaAnteriorId)
                .orElseThrow(() -> new EntityNotFoundException("No existe licencia con ID " + licenciaAnteriorId));

        Titular t = anterior.getTitular();

        // 2. Desactivar todas las licencias vigentes de este titular
        List<Licencia> vigentesPrevias = licenciaRepository.findByTitularAndVigente(t, true);
        for (Licencia prev : vigentesPrevias) {
            prev.setVigente(false);
            licenciaRepository.save(prev);
        }

        // 3. Validar edad mínima y, si corresponde, requisito profesional para la nueva
        // clase
        validarEdadMinima(t, nuevaClase);
        validarProfesional(t, nuevaClase);

        // 4. Calcular fecha de emisión y vencimiento para la nueva licencia
        LocalDate hoy = LocalDate.now();
        boolean esPrimeraVez = licenciaRepository.countByTitularAndClaseAndVigente(t, nuevaClase, true) == 0;
        LocalDate vence = calcularVencimiento(t, nuevaClase, hoy, esPrimeraVez);

        int aniosVigencia = Period.between(hoy, vence).getYears();
        BigDecimal costo = calcularCostoLicencia(nuevaClase, aniosVigencia);

        // 5. Crear la nueva licencia y dejarla vigente
        Licencia nueva = new Licencia();
        nueva.setTitular(t);
        nueva.setClase(nuevaClase);
        nueva.setFechaEmision(hoy);
        nueva.setFechaVencimiento(vence);
        nueva.setObservaciones(obs);
        nueva.setUsuarioAdmin(usuarioAdmin);
        nueva.setVigente(true);
        nueva.setTipoEmision(tipoEmision.RENOVACION);
        nueva.setCosto(costo);

        return licenciaRepository.save(nueva);
    }

    public List<Licencia> listarHistorialPorTitular(Long titularId) {
        Titular t = titularRepository.findById(titularId)
                .orElseThrow(() -> new EntityNotFoundException("Titular no encontrado con ID " + titularId));

        return licenciaRepository.findByTitularOrderByFechaEmisionDesc(t);
    }

    public Licencia emitirDuplicado(Long titularId, ClaseLicencia clase, String observaciones, String usuarioAdmin) {
        Titular titular = titularRepository.findById(titularId)
                .orElseThrow(() -> new EntityNotFoundException("Titular no encontrado para emitir duplicado."));

        // Validaciones: Las mismas validaciones de edad y profesional aplican para el
        // duplicado
        validarEdadMinima(titular, clase);
        validarProfesional(titular, clase);

        List<Licencia> vigentesPrevias = licenciaRepository.findByTitularAndVigente(titular, true);
        for (Licencia prev : vigentesPrevias) {
            prev.setVigente(false);
            licenciaRepository.save(prev);
        }

        // Determinar si esta es la primera vez que se emite esta clase de licencia
        // (para el cálculo de vencimiento)
        boolean esPrimeraVez = licenciaRepository.countByTitularAndClaseAndVigente(titular, clase, true) == 0;
        LocalDate hoy = LocalDate.now();
        LocalDate vence = calcularVencimiento(titular, clase, hoy, esPrimeraVez);

        int aniosVigencia = Period.between(hoy, vence).getYears();
        BigDecimal costo = calcularCostoLicencia(clase, aniosVigencia);

        Licencia duplicadoLicencia = new Licencia();
        duplicadoLicencia.setTitular(titular);
        duplicadoLicencia.setClase(clase);
        duplicadoLicencia.setFechaEmision(hoy);
        duplicadoLicencia.setFechaVencimiento(vence);
        duplicadoLicencia.setObservaciones(
                observaciones != null && !observaciones.trim().isEmpty() ? observaciones : "DUPLICADO DE LICENCIA");
        duplicadoLicencia.setUsuarioAdmin(usuarioAdmin);
        duplicadoLicencia.setVigente(true); // El duplicado es la nueva licencia activa
        duplicadoLicencia.setTipoEmision(tipoEmision.DUPLICADO);
        duplicadoLicencia.setCosto(costo);


        return licenciaRepository.save(duplicadoLicencia);
    }

    private static final BigDecimal COSTO_ADMIN = BigDecimal.valueOf(8);

    public BigDecimal calcularCostoLicencia(ClaseLicencia clase, int anios) {
        return clase.tarifaParaVigencia(anios).add(COSTO_ADMIN);
    }

    // Métodos para licencias expiradas
    public List<Licencia> listarLicenciasExpiradas() {
        return licenciaRepository.findByFechaVencimientoBeforeOrderByFechaVencimientoDesc(LocalDate.now());
    }

    public List<Licencia> filtrarLicenciasExpiradas(String usuarioAdmin, ClaseLicencia clase, LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate hoy = LocalDate.now();

        // Sin filtros
        if ((usuarioAdmin == null || usuarioAdmin.isEmpty()) && clase == null && fechaDesde == null && fechaHasta == null) {
            return listarLicenciasExpiradas();
        }

        // Solo filtro de usuario
        if ((usuarioAdmin != null && !usuarioAdmin.isEmpty()) && clase == null && fechaDesde == null && fechaHasta == null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseOrderByFechaVencimientoDesc(hoy, usuarioAdmin);
        }

        // Solo filtro de clase
        if ((usuarioAdmin == null || usuarioAdmin.isEmpty()) && clase != null && fechaDesde == null && fechaHasta == null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndClaseOrderByFechaVencimientoDesc(hoy, clase);
        }

        // Solo filtro de fechas
        if ((usuarioAdmin == null || usuarioAdmin.isEmpty()) && clase == null && fechaDesde != null && fechaHasta != null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(hoy, fechaDesde, fechaHasta);
        }

        // Filtro de usuario y clase
        if ((usuarioAdmin != null && !usuarioAdmin.isEmpty()) && clase != null && fechaDesde == null && fechaHasta == null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndClaseOrderByFechaVencimientoDesc(hoy, usuarioAdmin, clase);
        }

        // Filtro de usuario y fechas
        if ((usuarioAdmin != null && !usuarioAdmin.isEmpty()) && clase == null && fechaDesde != null && fechaHasta != null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(hoy, usuarioAdmin, fechaDesde, fechaHasta);
        }

        // Filtro de clase y fechas
        if ((usuarioAdmin == null || usuarioAdmin.isEmpty()) && clase != null && fechaDesde != null && fechaHasta != null) {
            return licenciaRepository.findByFechaVencimientoBeforeAndClaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(hoy, clase, fechaDesde, fechaHasta);
        }

        // Todos los filtros
        return licenciaRepository.findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndClaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(hoy, usuarioAdmin, clase, fechaDesde, fechaHasta);
    }

    // Métodos para licencias vigentes con filtros
    public List<Licencia> listarLicenciasVigentes() {
        return licenciaRepository.findByVigenteTrue();
    }

    public List<Licencia> filtrarLicenciasVigentes(String nombreApellido, GrupoSanguineo grupoSanguineo, FactorRh factorRh, Boolean donanteOrganos) {
        // Sin filtros
        if ((nombreApellido == null || nombreApellido.isEmpty()) && grupoSanguineo == null && factorRh == null && donanteOrganos == null) {
            return listarLicenciasVigentes();
        }

        // Solo filtro por nombre/apellido
        if ((nombreApellido != null && !nombreApellido.isEmpty()) && grupoSanguineo == null && factorRh == null && donanteOrganos == null) {
            return licenciaRepository.findByVigenteTrueAndTitular_ApellidoContainingIgnoreCaseOrVigenteTrueAndTitular_NombreContainingIgnoreCase(nombreApellido, nombreApellido);
        }

        // Solo filtro por grupo sanguíneo
        if ((nombreApellido == null || nombreApellido.isEmpty()) && grupoSanguineo != null && factorRh == null && donanteOrganos == null) {
            return licenciaRepository.findByVigenteTrueAndTitular_GrupoSanguineo(grupoSanguineo);
        }

        // Solo filtro por factor RH
        if ((nombreApellido == null || nombreApellido.isEmpty()) && grupoSanguineo == null && factorRh != null && donanteOrganos == null) {
            return licenciaRepository.findByVigenteTrueAndTitular_FactorRh(factorRh);
        }

        // Solo filtro por donante de órganos
        if ((nombreApellido == null || nombreApellido.isEmpty()) && grupoSanguineo == null && factorRh == null && donanteOrganos != null) {
            return licenciaRepository.findByVigenteTrueAndTitular_DonanteOrganos(donanteOrganos);
        }

        // Filtro por grupo sanguíneo y factor RH
        if ((nombreApellido == null || nombreApellido.isEmpty()) && grupoSanguineo != null && factorRh != null && donanteOrganos == null) {
            return licenciaRepository.findByVigenteTrueAndTitular_GrupoSanguineoAndTitular_FactorRh(grupoSanguineo, factorRh);
        }

        // Para combinaciones más complejas, podríamos usar Specification o QueryDSL
        // Por ahora, implementaremos una solución simple filtrando en memoria para las combinaciones restantes
        List<Licencia> resultado = listarLicenciasVigentes();

        if (nombreApellido != null && !nombreApellido.isEmpty()) {
            String busqueda = nombreApellido.toLowerCase();
            resultado = resultado.stream()
                    .filter(l -> l.getTitular().getNombre().toLowerCase().contains(busqueda) ||
                            l.getTitular().getApellido().toLowerCase().contains(busqueda))
                    .collect(Collectors.toList());
        }

        if (grupoSanguineo != null) {
            resultado = resultado.stream()
                    .filter(l -> l.getTitular().getGrupoSanguineo() == grupoSanguineo)
                    .collect(Collectors.toList());
        }

        if (factorRh != null) {
            resultado = resultado.stream()
                    .filter(l -> l.getTitular().getFactorRh() == factorRh)
                    .collect(Collectors.toList());
        }

        if (donanteOrganos != null) {
            resultado = resultado.stream()
                    .filter(l -> l.getTitular().getDonanteOrganos() == donanteOrganos)
                    .collect(Collectors.toList());
        }

        return resultado;
    }
}
