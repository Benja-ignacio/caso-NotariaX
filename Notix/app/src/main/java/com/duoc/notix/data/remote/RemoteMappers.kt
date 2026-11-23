package com.duoc.notix.data.remote

import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Aviso
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.DatosEmpresa
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.EventoHistorial
import com.duoc.notix.data.model.FormatoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.dto.AntecedentesDto
import com.duoc.notix.data.remote.dto.AvisoDto
import com.duoc.notix.data.remote.dto.CambioEstadoRequestDto
import com.duoc.notix.data.remote.dto.CrearAntecedentesDto
import com.duoc.notix.data.remote.dto.CrearDocumentoDto
import com.duoc.notix.data.remote.dto.CrearTramiteRequestDto
import com.duoc.notix.data.remote.dto.DocumentoDto
import com.duoc.notix.data.remote.dto.EventoHistorialDto
import com.duoc.notix.data.remote.dto.ReenvioRequestDto
import com.duoc.notix.data.remote.dto.RegistroUsuarioRequestDto
import com.duoc.notix.data.remote.dto.TramiteDto
import com.duoc.notix.data.remote.dto.UsuarioDto
import com.duoc.notix.util.fechaIsoAApp
import com.duoc.notix.util.fechaHoraActual

fun UsuarioDto.toDomain(contrasena: String = ""): Usuario = Usuario(
    id = id,
    tipoCuenta = enumOrDefault(tipoCuenta, TipoCuenta.PERSONA),
    nombreCompleto = nombreCompleto,
    correo = correo,
    contrasena = contrasena,
    run = run,
    numeroDocumento = numeroDocumento,
    telefono = telefono,
    comuna = comuna,
    empresa = razonSocialEmpresa?.let {
        DatosEmpresa(
            razonSocial = it,
            rutEmpresa = rutEmpresa.orEmpty(),
            giro = giroEmpresa.orEmpty()
        )
    },
    numeroNotaria = numeroNotaria
)

fun Usuario.toRegistroRequest(): RegistroUsuarioRequestDto = RegistroUsuarioRequestDto(
    tipoCuenta = tipoCuenta.name,
    nombreCompleto = nombreCompleto,
    correo = correo.trim().lowercase(),
    contrasena = contrasena,
    run = run,
    numeroDocumento = numeroDocumento,
    telefono = telefono,
    comuna = comuna,
    razonSocialEmpresa = empresa?.razonSocial,
    rutEmpresa = empresa?.rutEmpresa,
    giroEmpresa = empresa?.giro,
    numeroNotaria = numeroNotaria
)

fun TramiteDto.toDomain(): Tramite = Tramite(
    id = id,
    formato = FormatoTramite(
        id = formatoId,
        nombre = formatoNombre,
        categoria = formatoCategoria,
        firmaRequerida = enumOrDefault(tipoFirma, TipoFirma.SIMPLE)
    ),
    solicitanteId = solicitanteId,
    solicitanteNombre = solicitanteNombre,
    actuaEnNombreDe = actuaEnNombreDe,
    tipoFirma = enumOrDefault(tipoFirma, TipoFirma.SIMPLE),
    estado = enumOrDefault(estado, EstadoTramite.ENVIADO),
    documentos = documentos.map { it.toDomain() },
    antecedentes = antecedentes.toDomain(),
    observacion = observacion,
    legibilidadConfirmada = legibilidadConfirmada,
    firmaEvidencia = firmaEvidencia,
    historial = historial.map { it.toDomain() }
)

fun Tramite.toCrearRequest(): CrearTramiteRequestDto = CrearTramiteRequestDto(
    formatoId = formato.id,
    formatoNombre = formato.nombre,
    formatoCategoria = formato.categoria,
    solicitanteId = solicitanteId,
    solicitanteNombre = solicitanteNombre,
    actuaEnNombreDe = actuaEnNombreDe,
    tipoFirma = tipoFirma.name,
    firmaEvidencia = firmaEvidencia.orEmpty(),
    antecedentes = antecedentes.toRequest(),
    documentos = documentos.map { it.toRequest() }
)

fun Tramite.toCambioEstadoRequest(
    nuevoEstado: EstadoTramite,
    observacion: String?,
    legibilidadConfirmada: Boolean,
    rol: TipoCuenta,
    comentario: String? = null
): CambioEstadoRequestDto = CambioEstadoRequestDto(
    estado = nuevoEstado.name,
    observacion = observacion,
    legibilidadConfirmada = legibilidadConfirmada,
    rol = rol.name,
    comentario = comentario
)

fun Tramite.toReenvioRequest(
    antecedentes: Antecedentes,
    documentos: List<Documento>,
    comentario: String
): ReenvioRequestDto = ReenvioRequestDto(
    antecedentes = antecedentes.toRequest(),
    documentos = documentos.map { it.toRequest() },
    comentario = comentario.ifBlank { null }
)

fun AvisoDto.toDomain(): Aviso = Aviso(
    id = id,
    destinatarioId = destinatarioId,
    tramiteId = tramiteId,
    mensaje = mensaje,
    fechaHora = fechaIsoAApp(fechaHora).ifBlank { fechaHoraActual() },
    leido = leido
)

private fun AntecedentesDto.toDomain(): Antecedentes = Antecedentes(
    nombreCompleto = nombreCompleto,
    documentoIdentidad = documentoIdentidad,
    correoContacto = correoContacto,
    comuna = comuna
)

private fun DocumentoDto.toDomain(): Documento = Documento(
    nombre = nombre,
    tamanoKb = tamanoKb,
    estado = enumOrDefault(estado, EstadoDocumento.ACEPTADO)
)

private fun EventoHistorialDto.toDomain(): EventoHistorial = EventoHistorial(
    descripcion = descripcion,
    fechaHora = fechaIsoAApp(fechaHora),
    rol = enumOrDefault(rol, TipoCuenta.PERSONA)
)

private fun Antecedentes.toRequest(): CrearAntecedentesDto = CrearAntecedentesDto(
    nombreCompleto = nombreCompleto,
    documentoIdentidad = documentoIdentidad,
    correoContacto = correoContacto,
    comuna = comuna
)

private fun Documento.toRequest(): CrearDocumentoDto = CrearDocumentoDto(
    nombre = nombre,
    tamanoKb = tamanoKb,
    estado = estado.name
)

private inline fun <reified T : Enum<T>> enumOrDefault(valor: String?, default: T): T =
    runCatching { enumValueOf<T>(valor.orEmpty().uppercase()) }.getOrDefault(default)
