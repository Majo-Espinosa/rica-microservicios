package com.rica.investigadoresservice.investigadores.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorreoInstitucionalTest {

    @Test
    void noPermiteUnCorreoMalFormado() {
        assertThatThrownBy(() -> new CorreoInstitucional("no-es-un-correo"))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
