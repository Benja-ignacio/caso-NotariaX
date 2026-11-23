package com.duoc.notix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.Aviso
import com.duoc.notix.data.remote.TramitesApiProvider
import com.duoc.notix.repository.TramiteRepository
import com.duoc.notix.util.NotificacionUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AvisosViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String,
    private val onNuevosAvisos: (List<Aviso>) -> Unit = {}
) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.observarAvisos(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val nuevos = runCatching { repo.refrescarAvisos(usuarioId) }
                .getOrDefault(emptyList())
            onNuevosAvisos(nuevos)
        }
    }

    fun marcarComoLeido(avisoId: String) {
        viewModelScope.launch {
            repo.marcarAvisoLeido(avisoId)
        }
    }

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                val appContext = context.applicationContext
                AvisosViewModel(
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
