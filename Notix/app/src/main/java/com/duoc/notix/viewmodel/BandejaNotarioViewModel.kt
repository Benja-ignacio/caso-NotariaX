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
import com.duoc.notix.data.model.EstadoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.remote.TramitesApiProvider
import com.duoc.notix.repository.TramiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BandejaNotarioUiState(
    val cargando: Boolean = true,
    val nombreNotario: String = "",
    val numeroNotaria: Int = 1,
    val tramites: List<Tramite> = emptyList(),
    val enRevision: Int = 0,
    val observados: Int = 0,
    val finalizados: Int = 0,
    val filtro: FiltroTramites = FiltroTramites.TODOS
)

class BandejaNotarioViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroTramites.TODOS)

    init {
        viewModelScope.launch {
            runCatching { repo.refrescarTodosLosTramites() }
        }
    }

    var errorOperacion by mutableStateOf<String?>(null)
        private set

    val uiState: StateFlow<BandejaNotarioUiState> = combine(
        repo.observarUsuario(usuarioId),
        repo.observarTodosLosTramites(),
        filtro
    ) { usuario, todos, f ->
        val ordenados = todos.sortedByDescending { it.id }
        BandejaNotarioUiState(
            cargando = false,
            nombreNotario = usuario?.nombreCompleto ?: "Notario Demo",
            numeroNotaria = usuario?.numeroNotaria ?: 1,
            tramites = when (f) {
                FiltroTramites.TODOS -> ordenados
                FiltroTramites.EN_CURSO -> ordenados.filter { it.estado == EstadoTramite.ENVIADO }
                FiltroTramites.OBSERVADOS -> ordenados.filter { it.estado == EstadoTramite.DEVUELTO }
                FiltroTramites.FINALIZADOS -> ordenados.filter { it.estado == EstadoTramite.FINALIZADO }
            },
            enRevision = todos.count { it.estado == EstadoTramite.ENVIADO },
            observados = todos.count { it.estado == EstadoTramite.DEVUELTO },
            finalizados = todos.count { it.estado == EstadoTramite.FINALIZADO },
            filtro = f
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BandejaNotarioUiState())

    fun onFiltro(nuevo: FiltroTramites) {
        filtro.value = nuevo
    }

    fun limpiarError() {
        errorOperacion = null
    }

    fun aprobarTramite(tramite: Tramite, legibilidadConfirmada: Boolean) {
        if (!legibilidadConfirmada) {
            errorOperacion = "Debes confirmar que los documentos son legibles antes de aprobar."
            return
        }
        if (tramite.estado != EstadoTramite.ENVIADO) {
            errorOperacion = "Solo se pueden aprobar trámites en estado ENVIADO."
            return
        }
        viewModelScope.launch {
            errorOperacion = null
            try {
                repo.actualizarEstadoTramite(
                    tramite = tramite,
                    nuevoEstado = EstadoTramite.FINALIZADO,
                    legibilidadConfirmada = true,
                    rolHistorial = TipoCuenta.NOTARIO
                )
            } catch (e: Exception) {
                errorOperacion = e.localizedMessage ?: "No se pudo aprobar el trámite."
            }
        }
    }

    fun devolverTramite(tramite: Tramite, observacion: String) {
        if (tramite.estado != EstadoTramite.ENVIADO) {
            errorOperacion = "Solo se pueden devolver trámites en estado ENVIADO."
            return
        }
        if (observacion.isBlank()) {
            errorOperacion = "Ingresa el motivo de devolución."
            return
        }
        viewModelScope.launch {
            errorOperacion = null
            try {
                repo.actualizarEstadoTramite(
                    tramite = tramite,
                    nuevoEstado = EstadoTramite.DEVUELTO,
                    observacion = observacion.trim(),
                    rolHistorial = TipoCuenta.NOTARIO
                )
            } catch (e: Exception) {
                errorOperacion = e.localizedMessage ?: "No se pudo devolver el trámite."
            }
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                BandejaNotarioViewModel(
                    TramiteRepository(
                        db.tramiteDao(),
                        db.avisoDao(),
                        db.usuarioDao(),
                        TramitesApiProvider.api
                    ),
                    usuarioId
                )
            }
        }
    }
}
