package com.duoc.notix.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.notix.data.repository.FeriadosRepository
import kotlinx.coroutines.launch

data class FeriadoUiState(
    val cargando: Boolean = true,
    val nombre: String? = null,
    val fecha: String? = null,
    val sinDatos: Boolean = false
)

class FeriadosViewModel(
    private val repo: FeriadosRepository = FeriadosRepository()
) : ViewModel() {

    var uiState by mutableStateOf(FeriadoUiState())
        private set

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            uiState = uiState.copy(cargando = true, sinDatos = false)
            val feriado = repo.obtenerProximoFeriado()
            uiState = if (feriado == null) {
                FeriadoUiState(cargando = false, sinDatos = true)
            } else {
                FeriadoUiState(
                    cargando = false,
                    nombre = feriado.nombre,
                    fecha = feriado.fecha
                )
            }
        }
    }
}
