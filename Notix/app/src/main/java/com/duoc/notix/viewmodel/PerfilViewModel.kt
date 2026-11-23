package com.duoc.notix.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.duoc.notix.data.local.NotixDatabase
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.TramitesApiProvider
import com.duoc.notix.repository.TramiteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PerfilViewModel(
    repo: TramiteRepository,
    usuarioId: String
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = repo.observarUsuario(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        fun factory(context: Context, usuarioId: String) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                PerfilViewModel(
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
