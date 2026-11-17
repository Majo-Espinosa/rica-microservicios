package com.rica.investigadoresservice.investigadores.aplicacion;

import com.rica.investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorFactoryTest {

    @Mock
    private RepositorioInvestigadores repositorioInvestigadores;

    @InjectMocks
    private InvestigadorFactory investigadorFactory;

    @Test
    void creaUnInvestigadorConCorreoNuevo() {
        when(repositorioInvestigadores.existeCorreo("carlos.ruiz@uptc.edu.co")).thenReturn(false);

        Investigador creado = investigadorFactory.crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC");

        assertThat(creado.getId()).isNull();
        assertThat(creado.getCorreoInstitucional().valor()).isEqualTo("carlos.ruiz@uptc.edu.co");
    }

    @Test
    void rechazaCorreoInstitucionalDuplicado() {
        when(repositorioInvestigadores.existeCorreo("carlos.ruiz@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorFactory.crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC"))
                .isInstanceOf(CorreoDuplicadoException.class);
    }

}
