package com.duoc.notix.data.model

data class Usuario(
    val id: String,
    val tipoCuenta: TipoCuenta,
    val nombreCompleto: String,          // ficticio
    val correo: String,                  // ficticio, ej: ana.demo@notariax.test
    val contrasena: String,              // solo demo; nunca real
    val run: String,                     // ficticio
    val numeroDocumento: String,         // ficticio
    val empresa: DatosEmpresa? = null    // solo si tipoCuenta == EMPRESA
)

data class DatosEmpresa(
    val razonSocial: String,             // ficticia, ej: "Empresa Demo SpA"
    val rutEmpresa: String,              // ficticio
    val cargoRepresentante: String       // ej: "Representante legal"
)