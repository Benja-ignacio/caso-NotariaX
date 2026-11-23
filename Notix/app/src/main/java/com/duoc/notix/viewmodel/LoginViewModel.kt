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
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.UsuariosApiProvider
import com.duoc.notix.data.repository.AuthRepository
import com.duoc.notix.data.repository.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    var error by mutableStateOf<String?>(null)
        private set

    fun ingresar(tipo: TipoCuenta, correo: String, clave: String, onOk: (Usuario) -> Unit) {
        viewModelScope.launch {
            when (val r = repo.login(tipo, correo, clave)) {
                is AuthResult.Success -> { error = null; onOk(r.usuario) }
                is AuthResult.Error -> error = r.message
            }
        }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                LoginViewModel(
                    AuthRepository(db.usuarioDao(), UsuariosApiProvider.api)
                )
            }
        }
    }
}