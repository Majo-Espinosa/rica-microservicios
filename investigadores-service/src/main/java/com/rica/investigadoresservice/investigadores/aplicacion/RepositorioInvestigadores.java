package com.rica.investigadoresservice.investigadores.aplicacion;

import com.rica.investigadoresservice.investigadores.dominio.Investigador;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (secundario): lo disena quien lo consume (la aplicacion),
 * no la tecnologia que lo implementa (JPA).
 */
public interface RepositorioInvestigadores {

    List<Investigador> listarTodos();

    Optional<Investigador> buscarPorId(Long id);

    boolean existeCorreo(String correoInstitucional);

    Investigador guardar(Investigador investigador);

    Optional<Investigador> buscarPorCorreo(String correoInstitucional);

    void eliminar(Investigador investigador);

}
