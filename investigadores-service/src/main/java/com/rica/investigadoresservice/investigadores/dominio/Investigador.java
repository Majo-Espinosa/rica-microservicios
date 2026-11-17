package com.rica.investigadoresservice.investigadores.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "investigadores")
public class Investigador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Embedded
    private CorreoInstitucional correoInstitucional;

    @Column(name = "grupo_investigacion", length = 150)
    private String grupoInvestigacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoInvestigador estado = EstadoInvestigador.ACTIVO;

    protected Investigador() {
        // requerido por JPA
    }

    public Investigador(Long id, String nombreCompleto, CorreoInstitucional correoInstitucional,
                        String grupoInvestigacion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correoInstitucional = correoInstitucional;
        this.grupoInvestigacion = grupoInvestigacion;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public CorreoInstitucional getCorreoInstitucional() {
        return correoInstitucional;
    }

    public String getGrupoInvestigacion() {
        return grupoInvestigacion;
    }

    public EstadoInvestigador getEstado() {
        return estado;
    }

    public boolean estaEnEliminacion() {
        return estado == EstadoInvestigador.EN_ELIMINACION;
    }

    public void marcarEnEliminacion() {
        this.estado = EstadoInvestigador.EN_ELIMINACION;
    }

}
