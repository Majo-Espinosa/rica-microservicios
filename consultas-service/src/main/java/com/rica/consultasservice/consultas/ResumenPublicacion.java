package com.rica.consultasservice.consultas;

/** Copia desnormalizada de una publicacion, embebida dentro de la ficha del investigador. */
public record ResumenPublicacion(String id, String titulo, String tipo, Integer anio) {
}
