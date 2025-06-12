package com.utn.santafe.gestion_licencias.model.titular;

import java.math.BigDecimal;

public enum ClaseLicencia {
    A(new int[]{40, 30, 25, 20}),
    B(new int[]{40, 30, 25, 20}),
    C(new int[]{47, 35, 30, 23}),
    D(new int[]{40, 30, 25, 20}),
    E(new int[]{59, 44, 39, 29}),
    F(new int[]{40, 30, 25, 20}),
    G(new int[]{40, 30, 25, 20});


    private final int[] tarifas;

    ClaseLicencia(int[] tarifas) {
        this.tarifas = tarifas;
    }

    public BigDecimal tarifaParaVigencia(int anios) {
        int idx = switch (anios) {
            case 5 -> 0;
            case 4 -> 1;
            case 3 -> 2;
            case 1 -> 3;
            default -> throw new IllegalArgumentException("Vigencia inválida: " + anios);
        };
        return BigDecimal.valueOf(tarifas[idx]);
    }
}