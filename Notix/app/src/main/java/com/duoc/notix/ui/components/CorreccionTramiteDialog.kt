package com.duoc.notix.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.notix.data.model.Antecedentes
import com.duoc.notix.data.model.EstadoDocumento
import com.duoc.notix.data.model.Tramite
import com.duoc.notix.data.validation.RutValidator
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.util.obtenerNombreArchivo

/**
 * Permite corregir antecedentes y reemplazar un documento observado antes de
 * reenviar el trámite al notario. Solo guarda metadatos del archivo.
 */
@Composable
fun CorreccionTramiteDialog(
    tramite: Tramite,
    error: String?,
    procesando: Boolean,
    onDismiss: () -> Unit,
    onReenviar: (Antecedentes, String?, String) -> Unit
) {
    val context = LocalContext.current
    var nombreCompleto by remember { mutableStateOf(tramite.antecedentes.nombreCompleto) }
    var run by remember { mutableStateOf(tramite.antecedentes.documentoIdentidad) }
    var correo by remember { mutableStateOf(tramite.antecedentes.correoContacto) }
    var comuna by remember { mutableStateOf(tramite.antecedentes.comuna) }
    var comentario by remember { mutableStateOf("") }
    var documentoNombre by remember { mutableStateOf<String?>(null) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    val selectorDocumento = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            documentoNombre = obtenerNombreArchivo(context, uri) ?: "documento_corregido.pdf"
        }
    }

    val documentosObservados = tramite.documentos.filter { it.estado == EstadoDocumento.OBSERVADO }

    AlertDialog(
        onDismissRequest = { if (!procesando) onDismiss() },
        containerColor = Color.White,
        title = {
            Text(
                "Corregir y reenviar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Trámite ${tramite.id} · ${tramite.formato.nombre}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Gris
                )

                if (!tramite.observacion.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NotixColors.AlertaFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Observación del notario: ${tramite.observacion}",
                            fontSize = 13.sp,
                            color = NotixColors.AlertaTexto,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = run,
                    onValueChange = { run = it },
                    label = { Text("RUN / Documento") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo de contacto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = { Text("Comuna") },
                    singleLine = true,
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NotixColors.Borde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (documentoNombre != null) Icons.Outlined.UploadFile else Icons.Outlined.Description,
                            contentDescription = null,
                            tint = if (documentoNombre != null) NotixColors.Brand else NotixColors.Gris
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                documentosObservados.joinToString { it.nombre }.ifBlank { "Documentación adjunta" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NotixColors.Tinta
                            )
                            Text(
                                documentoNombre ?: "Selecciona un archivo de reemplazo",
                                fontSize = 12.sp,
                                color = if (documentoNombre != null) NotixColors.Brand else NotixColors.Gris
                            )
                        }
                        OutlinedButton(
                            onClick = { selectorDocumento.launch("*/*") },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            )
                        ) {
                            Text("Elegir", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario de corrección (opcional)") },
                    colors = notixTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                val mensajeError = errorLocal ?: error
                if (mensajeError != null) {
                    Text(
                        mensajeError,
                        fontSize = 12.sp,
                        color = NotixColors.AlertaTexto,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val errorRun = RutValidator.validar(run.trim(), "RUN")
                    val mensaje = when {
                        nombreCompleto.isBlank() -> "Ingresa el nombre completo."
                        errorRun != null -> errorRun
                        correo.isBlank() || !correo.contains("@") -> "Ingresa un correo válido."
                        comuna.isBlank() -> "Ingresa la comuna."
                        documentosObservados.isNotEmpty() && documentoNombre == null ->
                            "Adjunta un nuevo documento para reemplazar el observado."
                        else -> null
                    }
                    if (mensaje != null) {
                        errorLocal = mensaje
                    } else {
                        errorLocal = null
                        onReenviar(
                            Antecedentes(
                                nombreCompleto = nombreCompleto.trim(),
                                documentoIdentidad = run.trim(),
                                correoContacto = correo.trim(),
                                comuna = comuna.trim()
                            ),
                            documentoNombre,
                            comentario.trim()
                        )
                    }
                },
                enabled = !procesando,
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                Text(if (procesando) "Reenviando..." else "Reenviar al notario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !procesando) {
                Text("Cancelar", color = NotixColors.Gris)
            }
        }
    )
}
