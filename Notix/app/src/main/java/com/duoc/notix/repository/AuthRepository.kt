package com.duoc.notix.data.repository

import com.duoc.notix.data.local.UsuarioDao
import com.duoc.notix.data.local.toDomain
import com.duoc.notix.data.local.toEntity
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.data.remote.ApiErrors
import com.duoc.notix.data.remote.UsuariosApiProvider
import com.duoc.notix.data.remote.api.UsuariosApi
import com.duoc.notix.data.remote.dto.LoginRequestDto
import com.duoc.notix.data.remote.toDomain
import com.duoc.notix.data.remote.toRegistroRequest
import java.io.IOException
import retrofit2.HttpException

sealed interface AuthResult {
    data class Success(val usuario: Usuario) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val api: UsuariosApi = UsuariosApiProvider.api
) {

    suspend fun registrar(usuario: Usuario): AuthResult {
        return try {
            val creado = api.registrar(usuario.toRegistroRequest())
                .toDomain(contrasena = usuario.contrasena)
            usuarioDao.upsertUsuario(creado.toEntity())
            AuthResult.Success(creado)
        } catch (error: HttpException) {
            AuthResult.Error(ApiErrors.mensaje(error))
        } catch (error: IOException) {
            AuthResult.Error("Sin conexión con el servidor. Verifica que los microservicios estén activos.")
        } catch (error: Exception) {
            AuthResult.Error(error.localizedMessage ?: "No se pudo crear la cuenta.")
        }
    }

    suspend fun login(tipo: TipoCuenta, correo: String, contrasena: String): AuthResult {
        val correoNormalizado = correo.trim().lowercase()
        return try {
            val usuario = api.login(
                LoginRequestDto(
                    tipoCuenta = tipo.name,
                    correo = correoNormalizado,
                    contrasena = contrasena
                )
            ).toDomain(contrasena = contrasena)
            usuarioDao.upsertUsuario(usuario.toEntity())
            AuthResult.Success(usuario)
        } catch (error: HttpException) {
            AuthResult.Error(ApiErrors.mensaje(error))
        } catch (error: IOException) {
            // Sin red se permite entrar con la última copia local, para que la
            // demo no quede bloqueada si el backend no está levantado.
            loginLocal(tipo, correoNormalizado, contrasena)
        } catch (error: Exception) {
            AuthResult.Error(error.localizedMessage ?: "No se pudo iniciar sesión.")
        }
    }

    private suspend fun loginLocal(
        tipo: TipoCuenta,
        correo: String,
        contrasena: String
    ): AuthResult {
        val usuario = usuarioDao.getUsuarioPorCorreo(correo)?.toDomain()
            ?: return AuthResult.Error(
                "Sin conexión con el servidor y no hay una cuenta local guardada."
            )
        if (usuario.tipoCuenta != tipo) {
            return AuthResult.Error("Esa cuenta no es de tipo ${tipo.etiqueta}.")
        }
        if (usuario.contrasena != contrasena) {
            return AuthResult.Error("Contraseña incorrecta.")
        }
        return AuthResult.Success(usuario)
    }
}
