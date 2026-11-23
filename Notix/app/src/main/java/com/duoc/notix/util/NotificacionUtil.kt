package com.duoc.notix.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.duoc.notix.MainActivity
import com.duoc.notix.R
import com.duoc.notix.data.model.Aviso

/**
 * Notificaciones locales de avisos. No usan FCM ni servicios externos:
 * se disparan cuando la app sincroniza y detecta avisos nuevos.
 */
object NotificacionUtil {

    private const val CANAL_ID = "avisos_tramites"
    private const val PREFS = "notix_notificaciones"
    private const val KEY_IDS = "avisos_notificados"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val canal = NotificationChannel(
            CANAL_ID,
            "Avisos de trámites",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Cambios en el estado de tus trámites"
        }
        manager.createNotificationChannel(canal)
    }

    fun notificarAvisos(context: Context, avisos: List<Aviso>) {
        if (avisos.isEmpty() || !puedeNotificar(context)) return
        crearCanal(context)

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val notificados = prefs.getStringSet(KEY_IDS, emptySet()).orEmpty().toMutableSet()
        val nuevos = avisos.filter { !it.leido && it.id !in notificados }
        if (nuevos.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        nuevos.forEach { aviso ->
            val notificacion = NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_notaria_logo)
                .setContentTitle("Actualización de trámite")
                .setContentText(aviso.mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(aviso.mensaje))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            NotificationManagerCompat.from(context).notify(aviso.id.hashCode(), notificacion)
            notificados.add(aviso.id)
        }

        // Evitar que el conjunto crezca indefinidamente.
        val recientes = notificados.toList().takeLast(200).toSet()
        prefs.edit().putStringSet(KEY_IDS, recientes).apply()
    }

    private fun puedeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
