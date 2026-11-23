package com.duoc.notix.repository

import com.duoc.notix.data.local.AvisoDao
import com.duoc.notix.data.local.AvisoEntity
import com.duoc.notix.data.local.EventoHistorialEntity
import com.duoc.notix.data.local.TramiteDao
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.local.toEntities
import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.util.fechaHoraActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class TramiteRepository(
    private val tramiteDao: TramiteDao,
    private val avisoDao: AvisoDao,
    private val usuarioDao: UsuarioDao
) {
    fun observarMisTramites(usuarioId: String): Flow<List<Tramite>> =
        tramiteDao.getTramitesPorSolicitante(usuarioId).map { lista -> lista.map { it.toDomain() } }

    fun observarAvisosSinLeer(usuarioId: String): Flow<Boolean> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { avisos -> avisos.any { !it.leido } }

    fun observarAvisos(usuarioId: String): Flow<List<com.duoc.notix.data.model.Aviso>> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    suspend fun marcarAvisoLeido(avisoId: String) {
        avisoDao.marcarAvisoLeido(avisoId)
    }

    suspend fun crearAviso(aviso: com.duoc.notix.data.model.Aviso) {
        avisoDao.insertAviso(aviso.toEntity())
    }

    fun observarUsuario(usuarioId: String): Flow<Usuario?> =
        usuarioDao.getUsuarioPorId(usuarioId).map { it?.toDomain() }

    suspend fun obtenerUsuario(usuarioId: String): Usuario? =
        usuarioDao.getUsuarioPorId(usuarioId).firstOrNull()?.toDomain()

    suspend fun crearTramite(tramite: Tramite) {
        val (te, de, he) = tramite.toEntities()
        tramiteDao.insertTramiteCompleto(te, de, he)
    }

    fun observarTodosLosTramites(): Flow<List<Tramite>> =
        tramiteDao.getTodosLosTramites().map { lista -> lista.map { it.toDomain() } }

    suspend fun actualizarEstadoTramite(
        tramite: Tramite,
        nuevoEstado: com.duoc.notix.data.model.EstadoTramite,
        observacion: String? = null,
        legibilidadConfirmada: Boolean = false,
        descripcionHistorial: String,
        rolHistorial: com.duoc.notix.data.model.TipoCuenta,
        mensajeAviso: String? = null
    ) {
        check(tramite.estado == EstadoTramite.ENVIADO) {
            "Solo se pueden revisar trámites en estado ENVIADO."
        }
        when (nuevoEstado) {
            EstadoTramite.FINALIZADO -> check(legibilidadConfirmada) {
                "Debes confirmar que los documentos son legibles antes de aprobar."
            }
            EstadoTramite.DEVUELTO -> check(!observacion.isNullOrBlank()) {
                "La devolución requiere una observación."
            }
            EstadoTramite.ENVIADO -> error("Transición de estado no permitida.")
        }

        val tramiteActualizado = tramite.copy(
            estado = nuevoEstado,
            observacion = if (nuevoEstado == EstadoTramite.DEVUELTO) observacion else null,
            legibilidadConfirmada = nuevoEstado == EstadoTramite.FINALIZADO && legibilidadConfirmada
        )
        val (te, _, _) = tramiteActualizado.toEntities()

        val nuevoEvento = EventoHistorialEntity(
            tramiteId = tramite.id,
            descripcion = descripcionHistorial,
            fechaHora = fechaHoraActual(),
            rol = rolHistorial
        )

        val aviso = mensajeAviso?.let {
            AvisoEntity(
                id = "AVS-${System.currentTimeMillis().toString().takeLast(6)}",
                destinatarioId = tramite.solicitanteId,
                tramiteId = tramite.id,
                mensaje = it,
                fechaHora = fechaHoraActual(),
                leido = false
            )
        }

        tramiteDao.actualizarEstadoCompleto(te, nuevoEvento, aviso)
    }

    /**
     * Corrige y reenvía un trámite DEVUELTO. El notario deberá revisarlo nuevamente.
     * Los documentos observados deben reemplazarse por uno nuevo.
     */
    suspend fun reenviarTramite(
        tramite: Tramite,
        antecedentes: Antecedentes,
        nuevoDocumento: Documento?,
        comentario: String
    ) {
        check(tramite.estado == EstadoTramite.DEVUELTO) {
            "Solo se pueden corregir trámites devueltos."
        }
        require(antecedentes.nombreCompleto.isNotBlank()) { "El nombre completo es obligatorio." }
        require(antecedentes.documentoIdentidad.isNotBlank()) { "El RUN es obligatorio." }
        require(antecedentes.correoContacto.isNotBlank()) { "El correo es obligatorio." }
        require(antecedentes.comuna.isNotBlank()) { "La comuna es obligatoria." }

        val documentosObservados = tramite.documentos.filter { it.estado == EstadoDocumento.OBSERVADO }
        check(!(documentosObservados.isNotEmpty() && nuevoDocumento == null)) {
            "Adjunta un nuevo documento para reemplazar el observado."
        }

        val documentos = when {
            nuevoDocumento != null ->
                tramite.documentos.filter { it.estado != EstadoDocumento.OBSERVADO } + nuevoDocumento
            documentosObservados.isNotEmpty() ->
                tramite.documentos.filter { it.estado != EstadoDocumento.OBSERVADO } +
                    documentosObservados.map { it.copy(estado = EstadoDocumento.ACEPTADO) }
            else -> tramite.documentos
        }

        val tramiteActualizado = tramite.copy(
            estado = EstadoTramite.ENVIADO,
            observacion = null,
            legibilidadConfirmada = false,
            antecedentes = antecedentes,
            documentos = documentos
        )
        val (te, de, _) = tramiteActualizado.toEntities()

        val detalleComentario = comentario.trim().ifBlank { "sin comentario adicional" }
        val nuevoEvento = EventoHistorialEntity(
            tramiteId = tramite.id,
            descripcion = "Corregido y reenviado por el solicitante: $detalleComentario",
            fechaHora = fechaHoraActual(),
            rol = TipoCuenta.PERSONA
        )

        val aviso = AvisoEntity(
            id = "AVS-${System.currentTimeMillis().toString().takeLast(6)}",
            destinatarioId = tramite.solicitanteId,
            tramiteId = tramite.id,
            mensaje = "Tu trámite ${tramite.id} (${tramite.formato.nombre}) fue corregido y reenviado al notario.",
            fechaHora = fechaHoraActual(),
            leido = false
        )

        tramiteDao.reenviarTramiteCompleto(te, de, nuevoEvento, aviso)
    }
}