package com.duoc.notix.data.remote.dto

data class ApiErrorDto(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
)

data class LoginRequestDto(
    val tipoCuenta: String,
    val correo: String,
    val contrasena: String
)

data class RegistroUsuarioRequestDto(
    val tipoCuenta: String,
    val nombreCompleto: String,
    val correo: String,
    val contrasena: String,
    val run: String,
    val numeroDocumento: String,
    val telefono: String,
    val comuna: String,
    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,
    val numeroNotaria: Int? = null
)

data class UsuarioDto(
    val id: String = "",
    val tipoCuenta: String = "PERSONA",
    val nombreCompleto: String = "",
    val correo: String = "",
    val run: String = "",
    val numeroDocumento: String = "",
    val telefono: String = "",
    val comuna: String = "",
    val razonSocialEmpresa: String? = null,
    val rutEmpresa: String? = null,
    val giroEmpresa: String? = null,
    val numeroNotaria: Int? = null
)
