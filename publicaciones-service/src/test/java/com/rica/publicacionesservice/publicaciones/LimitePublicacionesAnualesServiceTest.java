package com.rica.publicacionesservice.publicaciones;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimitePublicacionesAnualesServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @InjectMocks
    private LimitePublicacionesAnualesService limiteService;

    @Test
    void permiteRegistrarMientrasNoSeAlcanceElMaximo() {
        when(publicacionRepository.countByInvestigadorCorreoAndAnio("ana.torres@uptc.edu.co", 2025)).thenReturn(2L);

        assertThat(limiteService.puedeRegistrar("ana.torres@uptc.edu.co", 2025)).isTrue();
    }

    @Test
    void rechazaCuandoYaSeAlcanzoElMaximoDelAnio() {
        when(publicacionRepository.countByInvestigadorCorreoAndAnio("ana.torres@uptc.edu.co", 2025)).thenReturn(3L);

        assertThat(limiteService.puedeRegistrar("ana.torres@uptc.edu.co", 2025)).isFalse();
    }

}
