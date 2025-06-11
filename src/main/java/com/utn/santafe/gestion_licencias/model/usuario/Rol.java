package com.utn.santafe.gestion_licencias.model.usuario;

public enum Rol {
    SUPERUSER("Super Usuario"),
    ADMIN("Administrador");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
