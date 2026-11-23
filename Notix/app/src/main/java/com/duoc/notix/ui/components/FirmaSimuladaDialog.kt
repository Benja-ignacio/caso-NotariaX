package com.duoc.notix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.notix.data.model.TipoFirma
import com.duoc.notix.ui.theme.NotixColors

/**
 * Paso de firma simulado previo al envío. No captura firmas, certificados ni
 * credenciales reales; solo genera una evidencia ficticia para la demo.
 */
@Composable
fun FirmaSimuladaDialog(
    tipoFirma: TipoFirma,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    val puntos = remember { mutableStateListOf<Offset>() }
    var token by remember { mutableStateOf("") }
    var autorizacionNotarial by remember { mutableStateOf(false) }

    val evidencia = when (tipoFirma) {
        TipoFirma.SIMPLE -> "SIMPLE_CANVAS"
        TipoFirma.AVANZADA -> "AVANZADA_TOKEN_SIMULADO"
        TipoFirma.NOTARIADA -> "NOTARIADA_SIMULADA"
    }
    val puedeConfirmar = when (tipoFirma) {
        TipoFirma.SIMPLE -> puntos.size >= 2
        TipoFirma.AVANZADA -> token.length == 6
        TipoFirma.NOTARIADA -> autorizacionNotarial
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Firma simulada",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    tipoFirma.etiqueta,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NotixColors.Tinta
                )
                Text(
                    tipoFirma.descripcion,
                    fontSize = 13.sp,
                    color = NotixColors.Gris
                )

                when (tipoFirma) {
                    TipoFirma.SIMPLE -> {
                        Text(
                            "Dibuja un trazo ficticio dentro del recuadro.",
                            fontSize = 13.sp,
                            color = NotixColors.Tinta
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, NotixColors.Borde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(Color.White)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { puntos.add(it) },
                                            onDrag = { change, _ ->
                                                puntos.add(change.position)
                                                change.consume()
                                            }
                                        )
                                    }
                            ) {
                                puntos.zipWithNext().forEach { (inicio, fin) ->
                                    drawLine(
                                        color = NotixColors.Tinta,
                                        start = inicio,
                                        end = fin,
                                        strokeWidth = 4.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                        TextButton(onClick = { puntos.clear() }) {
                            Text("Limpiar trazo", color = NotixColors.Gris)
                        }
                    }

                    TipoFirma.AVANZADA -> {
                        Text(
                            "Ingresa un código ficticio de 6 dígitos o usa el código demo.",
                            fontSize = 13.sp,
                            color = NotixColors.Tinta
                        )
                        OutlinedTextField(
                            value = token,
                            onValueChange = { nuevo ->
                                token = nuevo.filter { it.isDigit() }.take(6)
                            },
                            label = { Text("Código e-token simulado") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = notixTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(onClick = { token = "123456" }) {
                            Text("Usar código demo")
                        }
                    }

                    TipoFirma.NOTARIADA -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NotixColors.Fondo,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = autorizacionNotarial,
                                    onCheckedChange = { autorizacionNotarial = it }
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Confirmo la autorización notarial simulada para este trámite.",
                                    fontSize = 13.sp,
                                    color = NotixColors.Tinta
                                )
                            }
                        }
                    }
                }

                Text(
                    "Simulación académica: no se usan firmas, certificados ni credenciales reales.",
                    fontSize = 11.sp,
                    color = NotixColors.Gris
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(evidencia) },
                enabled = puedeConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
            ) {
                Text("Confirmar firma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = NotixColors.Gris)
            }
        }
    )
}
