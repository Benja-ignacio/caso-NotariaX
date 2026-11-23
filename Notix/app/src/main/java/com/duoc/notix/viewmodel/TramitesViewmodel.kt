package com.duoc.notix.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.Aviso
import com.duoc.notix.data.model.Documento
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.remote.TramitesApiProvider
import com.duoc.notix.data.validation.RutValidator
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.util.NotificacionUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroTramites(val etiqueta: String) {
    TODOS("Todos"),
    EN_CURSO("En curso"),
    OBSERVADOS("Observados"),
    FINALIZADOS("Finalizados")
}

data class MisTramitesUiState(
    val cargando: Boolean = true,
    val perfil: String = "",
    val nombre: String = "",
    val tramites: List<Tramite> = emptyList(),   // ya filtrados y ordenados
    val enRevision: Int = 0,
    val observados: Int = 0,
    val finalizados: Int = 0,
    val alerta: Tramite? = null,                 // trámite devuelto que requiere atención
    val filtro: FiltroTramites = FiltroTramites.TODOS,
    val avisosSinLeer: Boolean = false
)

class MisTramitesViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String,
    private val onNuevosAvisos: (List<Aviso>) -> Unit = {}
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroTramites.TODOS)

    init {
        viewModelScope.launch {
            runCatching { repo.refrescarMisTramites(usuarioId) }
            val nuevos = runCatching { repo.refrescarAvisos(usuarioId) }
                .getOrDefault(emptyList())
            onNuevosAvisos(nuevos)
        }
    }

    var errorOperacion by mutableStateOf<String?>(null)
        private set
    var operacionEnCurso by mutableStateOf(false)
        private set

    val uiState: StateFlow<MisTramitesUiState> = combine(
        repo.observarUsuario(usuarioId),
        repo.observarMisTramites(usuarioId),
        repo.observarAvisosSinLeer(usuarioId),
        filtro
    ) { usuario, todos, sinLeer, f ->
        val ordenados = todos.sortedByDescending { it.id }
        MisTramitesUiState(
            cargando = false,
            perfil = when (usuario?.tipoCuenta) {
                TipoCuenta.PERSONA -> "Persona natural"
                TipoCuenta.EMPRESA -> "Empresa"
                TipoCuenta.NOTARIO -> "Notario"
                null -> ""
            },
            nombre = usuario?.nombreCompleto.orEmpty().substringBefore(" "),
            tramites = when (f) {
                FiltroTramites.TODOS -> ordenados
                FiltroTramites.EN_CURSO -> ordenados.filter { it.estado != EstadoTramite.FINALIZADO }
                FiltroTramites.OBSERVADOS -> ordenados.filter { it.estado == EstadoTramite.DEVUELTO }
                FiltroTramites.FINALIZADOS -> ordenados.filter { it.estado == EstadoTramite.FINALIZADO }
            },

            enRevision = todos.count { it.estado == EstadoTramite.ENVIADO },
            observados = todos.count { it.estado == EstadoTramite.DEVUELTO },
            finalizados = todos.count { it.estado == EstadoTramite.FINALIZADO },
            alerta = ordenados.firstOrNull { it.estado == EstadoTramite.DEVUELTO },
            filtro = f,
            avisosSinLeer = sinLeer
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MisTramitesUiState())

    fun onFiltro(nuevo: FiltroTramites) { filtro.value = nuevo }

    fun limpiarErrorOperacion() {
        errorOperacion = null
    }

    fun reenviarTramite(
        tramite: Tramite,
        antecedentes: Antecedentes,
        nuevoDocumentoNombre: String?,
        comentario: String,
        onExito: () -> Unit
    ) {
        if (operacionEnCurso) return
        if (tramite.estado != EstadoTramite.DEVUELTO) {
            errorOperacion = "Solo se pueden corregir trámites devueltos."
            return
        }
        if (antecedentes.nombreCompleto.isBlank()) {
            errorOperacion = "Ingresa el nombre completo."
            return
        }
        RutValidator.validar(antecedentes.documentoIdentidad.trim(), "RUN")?.let {
            errorOperacion = it
            return
        }
        if (antecedentes.correoContacto.isBlank() || !antecedentes.correoContacto.contains("@")) {
            errorOperacion = "Ingresa un correo válido."
            return
        }
        if (antecedentes.comuna.isBlank()) {
            errorOperacion = "Ingresa la comuna."
            return
        }
        val documentosObservados = tramite.documentos.any { it.estado == EstadoDocumento.OBSERVADO }
        if (documentosObservados && nuevoDocumentoNombre.isNullOrBlank()) {
            errorOperacion = "Adjunta un nuevo documento para reemplazar el observado."
            return
        }

        viewModelScope.launch {
            operacionEnCurso = true
            errorOperacion = null
            try {
                val nuevoDocumento = nuevoDocumentoNombre
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        Documento(
                            nombre = it,
                            tamanoKb = (120..350).random(),
                            estado = EstadoDocumento.ACEPTADO
                        )
                    }
                repo.reenviarTramite(
                    tramite = tramite,
                    antecedentes = antecedentes.copy(
                        nombreCompleto = antecedentes.nombreCompleto.trim(),
                        documentoIdentidad = antecedentes.documentoIdentidad.trim(),
                        correoContacto = antecedentes.correoContacto.trim(),
                        comuna = antecedentes.comuna.trim()
                    ),
                    nuevoDocumento = nuevoDocumento,
                    comentario = comentario
                )
                runCatching { repo.refrescarAvisos(tramite.solicitanteId) }
                onExito()
            } catch (e: Exception) {
                errorOperacion = e.localizedMessage ?: "No se pudo reenviar el trámite."
            } finally {
                operacionEnCurso = false
            }
        }
    }

    fun eliminarTramite(tramite: Tramite, onExito: () -> Unit) {
        if (operacionEnCurso) return
        if (tramite.estado == EstadoTramite.FINALIZADO) {
            errorOperacion = "No se puede eliminar un trámite finalizado."
            return
        }

        viewModelScope.launch {
            operacionEnCurso = true
            errorOperacion = null
            try {
                repo.eliminarTramite(tramite)
                onExito()
            } catch (e: Exception) {
                errorOperacion = e.localizedMessage ?: "No se pudo eliminar el trámite."
            } finally {
                operacionEnCurso = false
            }
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                val appContext = context.applicationContext
                MisTramitesViewModel(
                    TramiteRepository(
                        db.tramiteDao(),
                        db.avisoDao(),
                        db.usuarioDao(),
                        TramitesApiProvider.api
                    ),
                    usuarioId
                ) { avisos -> NotificacionUtil.notificarAvisos(appContext, avisos) }
            }
        }
    }
}