package com.duoc.notix.tramites.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tramites")
public class Tramite {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Column(name = "formato_id", length = 10, nullable = false)
    private String formatoId;

    @Column(name = "formato_nombre", length = 120, nullable = false)
    private String formatoNombre;

    @Column(name = "formato_categoria", length = 80, nullable = false)
    private String formatoCategoria;

    @Column(name = "solicitante_id", length = 40, nullable = false)
    private String solicitanteId;

    @Column(name = "solicitante_nombre", length = 120, nullable = false)
    private String solicitanteNombre;

    @Column(name = "actua_en_nombre_de", length = 160)
    private String actuaEnNombreDe;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_firma", length = 20, nullable = false)
    private TipoFirma tipoFirma;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoTramite estado;

    @Embedded
    private Antecedentes antecedentes;

    @Column(length = 500)
    private String observacion;

    @Column(name = "legibilidad_confirmada", nullable = false)
    private boolean legibilidadConfirmada;

    @Column(name = "firma_evidencia", length = 60)
    private String firmaEvidencia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Documento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "tramite", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaHora ASC, id ASC")
    private List<EventoHistorial> historial = new ArrayList<>();

    public Tramite() {
    }

    public void agregarDocumento(Documento documento) {
        documento.setTramite(this);
        documentos.add(documento);
    }

    public void agregarEvento(EventoHistorial evento) {
        evento.setTramite(this);
        historial.add(evento);
    }

    public void reemplazarDocumentos(List<Documento> nuevos) {
        documentos.clear();
        nuevos.forEach(this::agregarDocumento);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormatoId() {
        return formatoId;
    }

    public void setFormatoId(String formatoId) {
        this.formatoId = formatoId;
    }

    public String getFormatoNombre() {
        return formatoNombre;
    }

    public void setFormatoNombre(String formatoNombre) {
        this.formatoNombre = formatoNombre;
    }

    public String getFormatoCategoria() {
        return formatoCategoria;
    }

    public void setFormatoCategoria(String formatoCategoria) {
        this.formatoCategoria = formatoCategoria;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(String solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getSolicitanteNombre() {
        return solicitanteNombre;
    }

    public void setSolicitanteNombre(String solicitanteNombre) {
        this.solicitanteNombre = solicitanteNombre;
    }

    public String getActuaEnNombreDe() {
        return actuaEnNombreDe;
    }

    public void setActuaEnNombreDe(String actuaEnNombreDe) {
        this.actuaEnNombreDe = actuaEnNombreDe;
    }

    public TipoFirma getTipoFirma() {
        return tipoFirma;
    }

    public void setTipoFirma(TipoFirma tipoFirma) {
        this.tipoFirma = tipoFirma;
    }

    public EstadoTramite getEstado() {
        return estado;
    }

    public void setEstado(EstadoTramite estado) {
        this.estado = estado;
    }

    public Antecedentes getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(Antecedentes antecedentes) {
        this.antecedentes = antecedentes;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public boolean isLegibilidadConfirmada() {
        return legibilidadConfirmada;
    }

    public void setLegibilidadConfirmada(boolean legibilidadConfirmada) {
        this.legibilidadConfirmada = legibilidadConfirmada;
    }

    public String getFirmaEvidencia() {
        return firmaEvidencia;
    }

    public void setFirmaEvidencia(String firmaEvidencia) {
        this.firmaEvidencia = firmaEvidencia;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public List<EventoHistorial> getHistorial() {
        return historial;
    }
}
