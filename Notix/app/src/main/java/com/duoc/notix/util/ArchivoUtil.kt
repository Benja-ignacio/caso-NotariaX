package com.duoc.notix.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/** Obtiene solo el nombre del archivo elegido; la app no conserva su contenido. */
fun obtenerNombreArchivo(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) return cursor.getString(nameIndex)
            }
        }
    }
    return uri.path?.substringAfterLast('/')
}
