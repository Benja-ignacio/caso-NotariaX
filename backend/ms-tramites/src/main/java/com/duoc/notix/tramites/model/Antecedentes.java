package com.duoc.notix.tramites.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Antecedentes {

    @Column(name = "antecedentes_nombre_completo", length = 120, nullable = false)
    private String nombreCompleto;

    @Column(name = "antecedentes_documento_identidad", length = 30, nullable = false)
    private String documentoIdentidad;

    @Column(name = "antecedentes_correo_contacto", length = 160, nullable = false)
    private String correoContacto;

    @Column(name = "antecedentes_comuna", length = 80, nullable = false)
    private String comuna;

    public Antecedentes() {
    }

    public Antecedentes(String nombreCompleto, String documentoIdentidad, String correoContacto, String comuna) {
        this.nombreCompleto = nombreCompleto;
        this.documentoIdentidad = documentoIdentidad;
        this.correoContacto = correoContacto;
        this.comuna = comuna;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }
}
