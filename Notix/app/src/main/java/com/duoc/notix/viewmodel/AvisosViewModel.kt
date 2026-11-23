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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AvisosViewModel(
    private val repo: TramiteRepository,
    private val usuarioId: String
) : ViewModel() {

    val avisos: StateFlow<List<Aviso>> = repo.observarAvisos(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            runCatching { repo.refrescarAvisos(usuarioId) }
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
                AvisosViewModel(
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
