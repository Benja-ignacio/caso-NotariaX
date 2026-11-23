package com.duoc.notix.data.remote

import com.duoc.notix.data.remote.dto.ApiErrorDto
import com.google.gson.Gson
import java.io.IOException
import retrofit2.HttpException

object ApiErrors {

    private val gson = Gson()

    fun mensaje(error: Throwable): String = when (error) {
        is HttpException -> mensajeHttp(error)
        is IOException -> "Sin conexión con el servidor. Se mantiene la última información guardada."
        else -> error.localizedMessage ?: "Ocurrió un error inesperado."
    }

    private fun mensajeHttp(error: HttpException): String {
        val mensaje = runCatching {
            error.response()?.errorBody()?.string()
                ?.let { gson.fromJson(it, ApiErrorDto::class.java)?.message }
        }.getOrNull()
        return mensaje?.takeIf { it.isNotBlank() } ?: "El servidor respondió con error ${error.code()}."
    }
}

/**
 * Ejecuta una llamada Retrofit y traduce sus fallos a mensajes que la UI
 * puede mostrar. La caché local no se modifica si la llamada falla.
 */
internal suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (error: HttpException) {
    throw IllegalStateException(ApiErrors.mensaje(error))
} catch (error: IOException) {
    throw IllegalStateException(ApiErrors.mensaje(error))
}
