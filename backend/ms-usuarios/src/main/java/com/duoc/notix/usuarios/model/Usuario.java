package com.duoc.notix.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 40, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", length = 20, nullable = false)
    private TipoCuenta tipoCuenta;

    @Column(name = "nombre_completo", length = 120, nullable = false)
    private String nombreCompleto;

    @Column(length = 160, nullable = false, unique = true)
    private String correo;

    @Column(length = 120, nullable = false)
    private String contrasena;

    @Column(length = 20, nullable = false)
    private String run;

    @Column(name = "numero_documento", length = 30, nullable = false)
    private String numeroDocumento;

    @Column(length = 30, nullable = false)
    private String telefono;

    @Column(length = 80, nullable = false)
    private String comuna;

    @Column(name = "razon_social_empresa", length = 160)
    private String razonSocialEmpresa;

    @Column(name = "rut_empresa", length = 20)
    private String rutEmpresa;

    @Column(name = "giro_empresa", length = 120)
    private String giroEmpresa;

    @Column(name = "numero_notaria")
    private Integer numeroNotaria;

    public Usuario() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoCuenta getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getRazonSocialEmpresa() {
        return razonSocialEmpresa;
    }

    public void setRazonSocialEmpresa(String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }

    public String getRutEmpresa() {
        return rutEmpresa;
    }

    public void setRutEmpresa(String rutEmpresa) {
        this.rutEmpresa = rutEmpresa;
    }

    public String getGiroEmpresa() {
        return giroEmpresa;
    }

    public void setGiroEmpresa(String giroEmpresa) {
        this.giroEmpresa = giroEmpresa;
    }

    public Integer getNumeroNotaria() {
        return numeroNotaria;
    }

    public void setNumeroNotaria(Integer numeroNotaria) {
        this.numeroNotaria = numeroNotaria;
    }
}
