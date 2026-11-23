package com.duoc.notix.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false)
    private Tramite tramite;

    @Column(length = 180, nullable = false)
    private String nombre;

    @Column(name = "tamano_kb", nullable = false)
    private Integer tamanoKb;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoDocumento estado;

    public Documento() {
    }

    public Documento(String nombre, Integer tamanoKb, EstadoDocumento estado) {
        this.nombre = nombre;
        this.tamanoKb = tamanoKb;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tramite getTramite() {
        return tramite;
    }

    public void setTramite(Tramite tramite) {
        this.tramite = tramite;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getTamanoKb() {
        return tamanoKb;
    }

    public void setTamanoKb(Integer tamanoKb) {
        this.tamanoKb = tamanoKb;
    }

    public EstadoDocumento getEstado() {
        return estado;
    }

    public void setEstado(EstadoDocumento estado) {
        this.estado = estado;
    }
}
