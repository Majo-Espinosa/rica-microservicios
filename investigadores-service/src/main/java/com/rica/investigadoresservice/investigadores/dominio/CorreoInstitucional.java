package com.rica.investigadoresservice.investigadores.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object: un correo institucional valido. No puede existir uno vacio ni mal formado,
 * asi que cualquier Investigador que lo tenga ya cumple la regla.
 */
@Embeddable
public record CorreoInstitucional(
        @Column(name = "correo_institucional", nullable = false, unique = true, length = 150)
        String valor) {

    private static final String FORMATO = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public CorreoInstitucional {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El correo institucional es obligatorio");
        }
        if (!valor.matches(FORMATO)) {
            throw new IllegalArgumentException("El correo institucional no tiene un formato válido: " + valor);
        }
    }

}
