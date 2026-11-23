package com.duoc.notix.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val meses = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

/** Devuelve la fecha y hora actual formateada como "dd/MM/yyyy HH:mm" */
fun fechaHoraActual(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

/**
 * Convierte una fecha ISO del backend (`2026-11-23T15:11:23.123`) al formato
 * visual de la app (`23/11/2026 15:11`). Si no se puede interpretar, devuelve
 * el texto original para no perder información.
 */
fun fechaIsoAApp(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val normalizada = iso.trim().substringBefore('.').removeSuffix("Z")
    val patrones = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm")
    for (patron in patrones) {
        val fecha = runCatching {
            SimpleDateFormat(patron, Locale.getDefault()).parse(normalizada)
        }.getOrNull()
        if (fecha != null) {
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(fecha)
        }
    }
    return iso
}

/** Convierte una fecha ISO sin hora (`2026-12-08`) a `08/12/2026`. */
fun fechaDiaAApp(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val normalizada = iso.trim().substringBefore('T')
    val fecha = runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(normalizada)
    }.getOrNull()
    return fecha?.let {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
    } ?: iso
}

/** "12/10/2026 10:00" -> "12 oct 2026". Si el formato no calza, devuelve el texto tal cual. */
fun fechaCorta(fechaHora: String?): String {
    if (fechaHora == null) return ""
    val partes = fechaHora.substringBefore(" ").split("/")
    if (partes.size != 3) return fechaHora
    val dia = partes[0].toIntOrNull() ?: return fechaHora
    val mes = partes[1].toIntOrNull()?.let { meses.getOrNull(it - 1) } ?: return fechaHora
    return "$dia $mes ${partes[2]}"
}