package com.rica.publicacionesservice.publicaciones;

/**
 * Puerto secundario: lo disena quien lo consume (publicaciones), no la tecnologia
 * que algun dia lo implemente para hablar con investigadores-service.
 */
public interface VerificadorInvestigador {

    boolean existe(String correoInstitucional);

}
