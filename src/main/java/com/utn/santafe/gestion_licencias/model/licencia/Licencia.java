package com.utn.santafe.gestion_licencias.model.licencia;

import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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

}



