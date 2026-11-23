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
import com.duoc.notix.data.model.*
import com.duoc.notix.data.remote.UsuariosApiProvider
import com.duoc.notix.data.repository.AuthRepository
import com.duoc.notix.data.repository.AuthResult
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    var errorRegistro by mutableStateOf<String?>(null)
        private set

    fun registrar(tipo: TipoCuenta, v: Map<String, String>, pass: String, onOk: (Usuario) -> Unit) {
        viewModelScope.launch {
            when (val r = repo.registrar(construirUsuario(tipo, v, pass))) {
                is AuthResult.Success -> { errorRegistro = null; onOk(r.usuario) }
                is AuthResult.Error -> errorRegistro = r.message
            }
        }
    }

    private fun construirUsuario(tipo: TipoCuenta, v: Map<String, String>, pass: String): Usuario {
        fun g(k: String) = v[k].orEmpty().trim()
        val id = "USR-" + UUID.randomUUID().toString().take(8).uppercase()
        return when (tipo) {
            TipoCuenta.PERSONA -> Usuario(
                id, tipo, g("nombre"), g("correo"), pass, g("rut"), g("doc"), g("telefono"), g("comuna")
            )
            TipoCuenta.EMPRESA -> Usuario(
                id, tipo, g("rep_nombre"), g("correo"), pass, g("rep_run"), g("rep_doc"), g("telefono"), g("comuna"),
                empresa = DatosEmpresa(g("razon"), g("rut"), g("giro"))
            )
            TipoCuenta.NOTARIO -> Usuario(
                id, tipo, g("nombre"), g("correo"), pass, g("run"), g("doc"), g("telefono"), g("notaria_comuna"),
                numeroNotaria = g("notaria_n").toIntOrNull()
            )
        }
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val db = NotixDatabase.getDatabase(context.applicationContext)
                AuthViewModel(
                    AuthRepository(db.usuarioDao(), UsuariosApiProvider.api)
                )
            }
        }
    }
}