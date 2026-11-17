package com.rica.investigadoresservice.investigadores.aplicacion;

import java.util.Map;

/** Puerto de salida: "publicar un evento", sin que la aplicacion conozca tablas ni Kafka. */
public interface PublicadorEventos {

    void publicar(String topico, String clave, Map<String, Object> contenido);

}
