package com.duoc.notix.data.remote.dto

data class FeriadoDto(
    val date: String = "",
    val localName: String = "",
    val name: String = "",
    val countryCode: String = "CL",
    val global: Boolean = true
)
