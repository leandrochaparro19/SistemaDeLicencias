package com.utn.santafe.gestion_licencias.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.FactorRh;
import com.utn.santafe.gestion_licencias.model.titular.GrupoSanguineo;
import com.utn.santafe.gestion_licencias.model.titular.Titular;

public interface LicenciaRepository extends JpaRepository<Licencia, Long> {

    Optional<Licencia> findByTitularAndClaseAndVigente(Titular t, ClaseLicencia c, boolean vigente);

    boolean existsByTitularAndClaseAndFechaEmisionBefore(Titular t, ClaseLicencia c, LocalDate fecha);

    long countByTitularAndClaseAndVigente(Titular t, ClaseLicencia c, boolean vigente);

    List<Licencia> findByTitularAndVigente(Titular t, boolean b);

    List<Licencia> findByTitularOrderByFechaEmisionDesc(Titular t);

    // Para licencias expiradas
    List<Licencia> findByFechaVencimientoBeforeOrderByFechaVencimientoDesc(LocalDate fecha);

    // Para filtros de licencias expiradas
    List<Licencia> findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseOrderByFechaVencimientoDesc(LocalDate fecha, String usuarioAdmin);
    List<Licencia> findByFechaVencimientoBeforeAndClaseOrderByFechaVencimientoDesc(LocalDate fecha, ClaseLicencia clase);
    List<Licencia> findByFechaVencimientoBeforeAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(LocalDate hoy, LocalDate desde, LocalDate hasta);
    List<Licencia> findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndClaseOrderByFechaVencimientoDesc(LocalDate fecha, String usuarioAdmin, ClaseLicencia clase);
    List<Licencia> findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(LocalDate hoy, String usuarioAdmin, LocalDate desde, LocalDate hasta);
    List<Licencia> findByFechaVencimientoBeforeAndClaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(LocalDate hoy, ClaseLicencia clase, LocalDate desde, LocalDate hasta);
    List<Licencia> findByFechaVencimientoBeforeAndUsuarioAdminContainingIgnoreCaseAndClaseAndFechaVencimientoBetweenOrderByFechaVencimientoDesc(LocalDate hoy, String usuarioAdmin, ClaseLicencia clase, LocalDate desde, LocalDate hasta);

    // Para licencias vigentes con filtros
    List<Licencia> findByVigenteTrue();
    List<Licencia> findByVigenteTrueAndTitular_ApellidoContainingIgnoreCaseOrVigenteTrueAndTitular_NombreContainingIgnoreCase(String apellido, String nombre);
    List<Licencia> findByVigenteTrueAndTitular_GrupoSanguineo(GrupoSanguineo grupoSanguineo);
    List<Licencia> findByVigenteTrueAndTitular_FactorRh(FactorRh factorRh);
    List<Licencia> findByVigenteTrueAndTitular_DonanteOrganos(Boolean donanteOrganos);
    List<Licencia> findByVigenteTrueAndTitular_GrupoSanguineoAndTitular_FactorRh(GrupoSanguineo grupoSanguineo, FactorRh factorRh);
}
