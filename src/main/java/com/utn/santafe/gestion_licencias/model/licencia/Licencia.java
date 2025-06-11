package com.utn.santafe.gestion_licencias.model.licencia;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Licencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Titular titular;

    @Enumerated(EnumType.STRING)
    private ClaseLicencia clase;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private String usuarioAdmin;

    private String observaciones;

    @Column(nullable = false)
    private boolean vigente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private tipoEmision TipoEmision;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal costo;
}
