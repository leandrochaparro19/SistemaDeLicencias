package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LicenciaRepository extends JpaRepository<Licencia, Long> {

    Optional<Licencia> findByTitularAndClaseAndVigente(Titular t, ClaseLicencia c, boolean vigente);
    boolean existsByTitularAndClaseAndFechaEmisionBefore(Titular t, ClaseLicencia c, LocalDate fecha);
    long     countByTitularAndClaseAndVigente(Titular t, ClaseLicencia c, boolean vigente);

    List<Licencia> findByTitularAndVigente(Titular t, boolean b);

    List<Licencia> findByTitularOrderByFechaEmisionDesc(Titular t);
}