package com.rica.publicacionesservice.publicaciones;

import org.springframework.stereotype.Service;

/**
 * Regla de negocio: un investigador puede registrar como máximo
 * {@value #MAXIMO_PUBLICACIONES_POR_ANIO} publicaciones por año.
 */
@Service
public class LimitePublicacionesAnualesService {

    static final int MAXIMO_PUBLICACIONES_POR_ANIO = 3;

    private final PublicacionRepository publicacionRepository;

    public LimitePublicacionesAnualesService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public boolean puedeRegistrar(String investigadorCorreo, Integer anio) {
        long registradas = publicacionRepository.countByInvestigadorCorreoAndAnio(investigadorCorreo, anio);
        return registradas < MAXIMO_PUBLICACIONES_POR_ANIO;
    }

}
