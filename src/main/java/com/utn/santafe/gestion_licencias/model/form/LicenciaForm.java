package com.utn.santafe.gestion_licencias.model.form;

import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LicenciaForm {
    @NotNull(message = "El titular no puede ser nulo")
    private Long titularId;

    @NotNull(message = "Debe seleccionar una clase de licencia")
    private ClaseLicencia clase;

    @Size(max = 255, message = "Las observaciones no pueden exceder 255 caracteres")
    private String observaciones;

}