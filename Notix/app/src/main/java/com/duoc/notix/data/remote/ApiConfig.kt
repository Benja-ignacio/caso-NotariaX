package com.duoc.notix.data.remote

import android.os.Build

/**
 * URLs locales de la demo.
 *
 * - Emulador: `10.0.2.2` es el host del PC.
 * - Dispositivo físico: `127.0.0.1` funciona junto con
 *   `adb reverse tcp:8081 tcp:8081` y `adb reverse tcp:8082 tcp:8082`.
 *   También se puede reemplazar por la IP LAN del PC.
 */
object ApiConfig {

    private val esEmulador: Boolean
        get() = Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")

    val USUARIOS_BASE_URL: String
        get() = if (esEmulador) "http://10.0.2.2:8081/" else "http://127.0.0.1:8081/"

    val TRAMITES_BASE_URL: String
        get() = if (esEmulador) "http://10.0.2.2:8082/" else "http://127.0.0.1:8082/"

    const val FERIADOS_BASE_URL = "https://date.nager.at/"
}
