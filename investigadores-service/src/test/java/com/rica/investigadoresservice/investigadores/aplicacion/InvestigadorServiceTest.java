package com.rica.investigadoresservice.investigadores.aplicacion;

import com.rica.investigadoresservice.compartido.RecursoNoEncontradoException;
import com.rica.investigadoresservice.investigadores.dominio.CorreoInstitucional;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorServiceTest {

    @Mock
    private RepositorioInvestigadores repositorioInvestigadores;

    @Mock
    private InvestigadorFactory investigadorFactory;

    @InjectMocks
    private InvestigadorService investigadorService;

    private static Investigador ana(Long id) {
        return new Investigador(id, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
    }

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(ana(1L)));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioInvestigadores.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void listarTodosDelegaEnElPuertoDeSalida() {
        when(repositorioInvestigadores.listarTodos()).thenReturn(List.of(ana(1L)));

        assertThat(investigadorService.listarTodos()).hasSize(1);
    }

    @Test
    void registrarGuardaElInvestigadorCreadoPorLaFabrica() {
        Investigador nuevo = ana(null);
        when(investigadorFactory.crear("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC")).thenReturn(nuevo);
        when(repositorioInvestigadores.guardar(nuevo)).thenReturn(ana(1L));

        Investigador guardado = investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(guardado.getId()).isEqualTo(1L);
        verify(repositorioInvestigadores).guardar(nuevo);
    }

}
