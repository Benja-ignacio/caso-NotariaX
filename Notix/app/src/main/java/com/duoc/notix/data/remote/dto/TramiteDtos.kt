package com.duoc.notix.data.remote.dto

data class AntecedentesDto(
    val nombreCompleto: String = "",
    val documentoIdentidad: String = "",
    val correoContacto: String = "",
    val comuna: String = ""
)

data class DocumentoDto(
    val id: Long? = null,
    val nombre: String = "",
    val tamanoKb: Int = 0,
    val estado: String = "ACEPTADO"
)

data class EventoHistorialDto(
    val id: Long? = null,
    val descripcion: String = "",
    val fechaHora: String? = null,
    val rol: String = "PERSONA"
)

data class TramiteDto(
    val id: String = "",
    val formatoId: String = "",
    val formatoNombre: String = "",
    val formatoCategoria: String = "",
    val solicitanteId: String = "",
    val solicitanteNombre: String = "",
    val actuaEnNombreDe: String? = null,
    val tipoFirma: String = "SIMPLE",
    val estado: String = "ENVIADO",
    val antecedentes: AntecedentesDto = AntecedentesDto(),
    val documentos: List<DocumentoDto> = emptyList(),
    val observacion: String? = null,
    val legibilidadConfirmada: Boolean = false,
    val firmaEvidencia: String? = null,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val historial: List<EventoHistorialDto> = emptyList()
)

data class CrearAntecedentesDto(
    val nombreCompleto: String,
    val documentoIdentidad: String,
    val correoContacto: String,
    val comuna: String
)

data class CrearDocumentoDto(
    val nombre: String,
    val tamanoKb: Int,
    val estado: String = "ACEPTADO"
)

data class CrearTramiteRequestDto(
    val formatoId: String,
    val formatoNombre: String,
    val formatoCategoria: String,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val actuaEnNombreDe: String? = null,
    val tipoFirma: String,
    val firmaEvidencia: String,
    val antecedentes: CrearAntecedentesDto,
    val documentos: List<CrearDocumentoDto>
)

data class CambioEstadoRequestDto(
    val estado: String,
    val observacion: String? = null,
    val legibilidadConfirmada: Boolean = false,
    val rol: String,
    val comentario: String? = null
)

data class ReenvioRequestDto(
    val antecedentes: CrearAntecedentesDto,
    val documentos: List<CrearDocumentoDto>,
    val comentario: String? = null
)

data class MarcarLeidoRequestDto(
    val leido: Boolean = true
)

data class AvisoDto(
    val id: String = "",
    val destinatarioId: String = "",
    val tramiteId: String = "",
    val mensaje: String = "",
    val fechaHora: String? = null,
    val leido: Boolean = false
)
