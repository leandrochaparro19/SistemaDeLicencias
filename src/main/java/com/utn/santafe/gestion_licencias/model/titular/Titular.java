package com.utn.santafe.gestion_licencias.model.titular;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Data
public class Titular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "\\d{1,8}", message = "Sólo dígitos (máximo 8)")
    private String numeroDocumento;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotNull(message = "La clase solicitada es obligatoria")
    @Enumerated(EnumType.STRING)
    private ClaseLicencia claseSolicitada;

    @NotNull(message = "El grupo sanguíneo es obligatorio")
    @Enumerated(EnumType.STRING)
    private GrupoSanguineo grupoSanguineo;

    @NotNull(message = "El factor RH es obligatorio")
    @Enumerated(EnumType.STRING)
    private FactorRh factorRh;

    @NotNull(message = "Debe indicar si es donante de órganos")
    private Boolean donanteOrganos;
}
