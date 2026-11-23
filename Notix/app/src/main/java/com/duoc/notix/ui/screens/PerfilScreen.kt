package com.duoc.notix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.data.model.Usuario
import com.duoc.notix.ui.components.BarraInferiorPersona
import com.duoc.notix.ui.theme.NotixColors
import com.duoc.notix.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuarioId: String,
    onVolver: () -> Unit,
    onAvisos: () -> Unit,
    onCerrarSesion: () -> Unit,
    vm: PerfilViewModel = viewModel(
        key = usuarioId,
        factory = PerfilViewModel.factory(LocalContext.current, usuarioId)
    )
) {
    val usuario by vm.usuario.collectAsState()
    var confirmarCierreSesion by remember { mutableStateOf(false) }

    if (confirmarCierreSesion) {
        AlertDialog(
            onDismissRequest = { confirmarCierreSesion = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Cerrar sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NotixColors.Tinta
                )
            },
            text = {
                Text(
                    "¿Quieres cerrar tu sesión y volver al inicio?",
                    fontSize = 14.sp,
                    color = NotixColors.Gris
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmarCierreSesion = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NotixColors.Brand)
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierreSesion = false }) {
                    Text("Cancelar", color = NotixColors.Gris)
                }
            }
        )
    }

    Scaffold(
        containerColor = NotixColors.Fondo,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi perfil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.Tinta
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = NotixColors.Tinta
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NotixColors.Fondo)
            )
        },
        bottomBar = {
            BarraInferiorPersona(
                seleccionado = 2,
                onTramites = onVolver,
                onAvisos = onAvisos,
                onPerfil = {}
            )
        }
    ) { padding ->
        if (usuario == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando perfil…", color = NotixColors.Gris)
            }
        } else {
            PerfilContent(
                usuario = usuario!!,
                onCerrarSesion = { confirmarCierreSesion = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun PerfilContent(
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, NotixColors.Borde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = NotixColors.Fondo,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            usuario.nombreCompleto.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotixColors.Brand
                        )
                    }
                }
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        usuario.nombreCompleto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NotixColors.Tinta,
                        fontFamily = FontFamily.Serif
                    )
                    Text(usuario.correo, fontSize = 13.sp, color = NotixColors.Gris)
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NotixColors.RevisionFondo
                    ) {
                        Text(
                            usuario.tipoCuenta.etiqueta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NotixColors.RevisionTexto,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        DatoCard(
            titulo = "Datos de la cuenta",
            datos = buildList {
                add("RUN" to usuario.run)
                add("Documento" to usuario.numeroDocumento)
                add("Teléfono" to usuario.telefono)
                add("Comuna" to usuario.comuna)
            }
        )

        if (usuario.tipoCuenta == TipoCuenta.EMPRESA && usuario.empresa != null) {
            DatoCard(
                titulo = "Datos de la empresa",
                datos = listOf(
                    "Razón social" to usuario.empresa.razonSocial,
                    "RUT empresa" to usuario.empresa.rutEmpresa,
                    "Giro" to usuario.empresa.giro
                )
            )
        }

        if (usuario.tipoCuenta == TipoCuenta.NOTARIO && usuario.numeroNotaria != null) {
            DatoCard(
                titulo = "Datos de la notaría",
                datos = listOf("Número de notaría" to usuario.numeroNotaria.toString())
            )
        }

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, NotixColors.Borde)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = NotixColors.Tinta
            )
            Spacer(Modifier.size(8.dp))
            Text("Cerrar sesión", color = NotixColors.Tinta, fontWeight = FontWeight.SemiBold)
        }

        Text(
            "La sesión se mantiene solo mientras la app está abierta. Las contraseñas no se muestran en esta pantalla.",
            fontSize = 11.sp,
            color = NotixColors.Gris
        )
    }
}

@Composable
private fun DatoCard(
    titulo: String,
    datos: List<Pair<String, String>>
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, NotixColors.Borde),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NotixColors.Tinta
            )
            Spacer(Modifier.height(10.dp))
            datos.forEachIndexed { index, (etiqueta, valor) ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        etiqueta,
                        fontSize = 12.sp,
                        color = NotixColors.Gris,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        valor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NotixColors.Tinta
                    )
                }
            }
        }
    }
}
