package com.duoc.notix.data.repository

import com.duoc.notix.data.remote.FeriadosApiProvider
import com.duoc.notix.data.remote.api.FeriadosApi
import com.duoc.notix.util.fechaDiaAApp

data class Feriado(
    val fecha: String,
    val nombre: String
)

/**
 * API externa opcional: cualquier fallo se convierte en `null` para que la app
 * muestre un respaldo y nunca bloquee el flujo de trámites.
 */
class FeriadosRepository(
    private val api: FeriadosApi = FeriadosApiProvider.api
) {
    suspend fun obtenerProximoFeriado(): Feriado? = runCatching {
        api.proximos("CL").firstOrNull()?.let { dto ->
            val nombre = dto.localName.ifBlank { dto.name }
            if (nombre.isBlank()) {
                null
            } else {
                Feriado(
                    fecha = fechaDiaAApp(dto.date),
                    nombre = nombre
                )
            }
        }
    }.getOrNull()
}
