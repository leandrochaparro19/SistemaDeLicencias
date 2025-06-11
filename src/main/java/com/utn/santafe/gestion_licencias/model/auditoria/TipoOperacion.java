package com.utn.santafe.gestion_licencias.model.auditoria;

public enum TipoOperacion {
    CREACION("Creación"),
    MODIFICACION("Modificación"),
    ELIMINACION("Eliminación"),
    ACTIVACION("Activación"),
    DESACTIVACION("Desactivación"),
    CAMBIO_PASSWORD("Cambio de Contraseña");

    private final String descripcion;

    TipoOperacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
