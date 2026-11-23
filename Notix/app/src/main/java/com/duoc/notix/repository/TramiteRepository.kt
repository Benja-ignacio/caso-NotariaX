package com.duoc.notix.repository

import com.duoc.notix.data.local.AvisoDao
import com.duoc.notix.data.local.TramiteDao
import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.local.toEntities
import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Aviso
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.ApiErrors
import com.duoc.notix.data.remote.TramitesApiProvider
import com.duoc.notix.data.remote.api.TramitesApi
import com.duoc.notix.data.remote.apiCall
import com.duoc.notix.data.remote.dto.MarcarLeidoRequestDto
import com.duoc.notix.data.remote.toCambioEstadoRequest
import com.duoc.notix.data.remote.toCrearRequest
import com.duoc.notix.data.remote.toDomain
import com.duoc.notix.data.remote.toReenvioRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class TramiteRepository(
    private val tramiteDao: TramiteDao,
    private val avisoDao: AvisoDao,
    private val usuarioDao: UsuarioDao,
    private val api: TramitesApi = TramitesApiProvider.api
) {
    fun observarMisTramites(usuarioId: String): Flow<List<Tramite>> =
        tramiteDao.getTramitesPorSolicitante(usuarioId).map { lista -> lista.map { it.toDomain() } }

    fun observarAvisosSinLeer(usuarioId: String): Flow<Boolean> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { avisos -> avisos.any { !it.leido } }

    fun observarAvisos(usuarioId: String): Flow<List<Aviso>> =
        avisoDao.getAvisosPorDestinatario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    fun observarUsuario(usuarioId: String): Flow<Usuario?> =
        usuarioDao.getUsuarioPorId(usuarioId).map { it?.toDomain() }

    suspend fun obtenerUsuario(usuarioId: String): Usuario? =
        usuarioDao.getUsuarioPorId(usuarioId).firstOrNull()?.toDomain()

    fun observarTodosLosTramites(): Flow<List<Tramite>> =
        tramiteDao.getTodosLosTramites().map { lista -> lista.map { it.toDomain() } }

    /**
     * Trae los trámites del solicitante desde el backend y actualiza Room.
     * Si el backend falla, la excepción queda en el ViewModel y la UI sigue
     * mostrando la última copia local.
     */
    suspend fun refrescarMisTramites(usuarioId: String) {
        val remotos = apiCall { api.porSolicitante(usuarioId) }
        remotos.forEach { guardarRemoto(it.toDomain()) }
    }

    suspend fun refrescarTodosLosTramites() {
        val remotos = apiCall { api.todos() }
        remotos.forEach { guardarRemoto(it.toDomain()) }
    }

    suspend fun refrescarAvisos(usuarioId: String) {
        val remotos = apiCall { api.avisos(usuarioId) }
        remotos.forEach { avisoDao.insertAviso(it.toDomain().toEntity()) }
    }

    suspend fun marcarAvisoLeido(avisoId: String) {
        // Se intenta sincronizar primero; si no hay red, el cambio local se
        // conserva para que la UI siga respondiendo durante la demo.
        runCatching {
            apiCall { api.marcarLeido(avisoId, MarcarLeidoRequestDto()) }
        }
        avisoDao.marcarAvisoLeido(avisoId)
    }

    suspend fun crearTramite(tramite: Tramite): Tramite {
        val creado = apiCall { api.crear(tramite.toCrearRequest()) }.toDomain()
        guardarRemoto(creado)
        return creado
    }

    suspend fun actualizarEstadoTramite(
        tramite: Tramite,
        nuevoEstado: EstadoTramite,
        observacion: String? = null,
        legibilidadConfirmada: Boolean = false,
        rolHistorial: TipoCuenta,
        comentario: String? = null
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

        val actualizado = apiCall {
            api.cambiarEstado(
                tramite.id,
                tramite.toCambioEstadoRequest(
                    nuevoEstado = nuevoEstado,
                    observacion = observacion,
                    legibilidadConfirmada = legibilidadConfirmada,
                    rol = rolHistorial,
                    comentario = comentario
                )
            )
        }.toDomain()
        guardarRemoto(actualizado)
    }

    /**
     * Corrige y reenvía un trámite DEVUELTO. El backend valida la transición,
     * reemplaza documentos y genera el historial y el aviso.
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

        val observados = tramite.documentos.filter { it.estado == EstadoDocumento.OBSERVADO }
        check(!(observados.isNotEmpty() && nuevoDocumento == null)) {
            "Adjunta un nuevo documento para reemplazar el observado."
        }

        val documentos = buildList {
            addAll(tramite.documentos.filter { it.estado != EstadoDocumento.OBSERVADO })
            if (nuevoDocumento != null) add(nuevoDocumento)
        }
        check(documentos.isNotEmpty()) {
            "El trámite debe conservar al menos un documento."
        }

        val actualizado = apiCall {
            api.reenviar(
                tramite.id,
                tramite.toReenvioRequest(
                    antecedentes = antecedentes,
                    documentos = documentos,
                    comentario = comentario
                )
            )
        }.toDomain()
        guardarRemoto(actualizado)
    }

    suspend fun eliminarTramite(tramite: Tramite) {
        check(tramite.estado != EstadoTramite.FINALIZADO) {
            "No se puede eliminar un trámite finalizado."
        }
        val response = apiCall { api.eliminar(tramite.id) }
        if (!response.isSuccessful) {
            throw IllegalStateException(ApiErrors.mensaje(HttpException(response)))
        }
        tramiteDao.eliminarTramiteLocal(tramite.id)
    }

    private suspend fun guardarRemoto(tramite: Tramite) {
        val (tramiteEntity, documentos, historial) = tramite.toEntities()
        tramiteDao.upsertTramiteRemoto(tramiteEntity, documentos, historial)
    }
}
