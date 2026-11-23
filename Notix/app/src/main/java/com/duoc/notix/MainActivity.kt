package com.duoc.notix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.duoc.notix.data.local.DatabaseSeeder
import com.duoc.notix.data.model.FormatoTramite
import com.duoc.notix.data.model.TipoCuenta
import com.duoc.notix.ui.screens.AvisosScreen
import com.duoc.notix.ui.screens.BandejaNotarioScreen
import com.duoc.notix.ui.screens.CatalogoFormatosScreen
import com.duoc.notix.ui.screens.CrearCuentaScreen
import com.duoc.notix.ui.screens.CrearTramiteScreen
import com.duoc.notix.ui.screens.LoginScreen
import com.duoc.notix.ui.screens.MisTramitesScreen
import com.duoc.notix.ui.screens.PerfilScreen
import com.duoc.notix.ui.screens.WelcomeScreen
import com.duoc.notix.ui.theme.NotixTheme

enum class Pantalla {
    Welcome,
    Login,
    CrearCuenta,
    MisTramites,
    CatalogoFormatos,
    CrearTramite,
    BandejaNotario,
    Avisos,
    Perfil
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotixTheme {
                LaunchedEffect(Unit) {
                    DatabaseSeeder.seedIfEmpty(applicationContext)
                }

                var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.Welcome) }
                var usuarioId by rememberSaveable { mutableStateOf("USR-001") }
                var formatoSeleccionado by androidx.compose.runtime.remember { mutableStateOf<FormatoTramite?>(null) }

                val cerrarSesion = {
                    usuarioId = ""
                    formatoSeleccionado = null
                    pantallaActual = Pantalla.Welcome
                }

                when (pantallaActual) {
                    Pantalla.Welcome -> WelcomeScreen(
                        onNavigateToLogin = { pantallaActual = Pantalla.Login },
                        onNavigateToRegister = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.Login -> LoginScreen(
                        onLoginSuccess = { usuario ->
                            usuarioId = usuario.id
                            if (usuario.tipoCuenta == TipoCuenta.NOTARIO) {
                                pantallaActual = Pantalla.BandejaNotario
                            } else {
                                pantallaActual = Pantalla.MisTramites
                            }
                        },
                        onCreateAccount = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.CrearCuenta -> CrearCuentaScreen(
                        onVolver = { pantallaActual = Pantalla.Welcome },
                        onIngresar = { pantallaActual = Pantalla.Login },
                        onRegistrado = { usuario ->
                            usuarioId = usuario.id
                            if (usuario.tipoCuenta == TipoCuenta.NOTARIO) {
                                pantallaActual = Pantalla.BandejaNotario
                            } else {
                                pantallaActual = Pantalla.MisTramites
                            }
                        }
                    )
                    Pantalla.MisTramites -> MisTramitesScreen(
                        usuarioId = usuarioId,
                        onNuevoTramite = { pantallaActual = Pantalla.CatalogoFormatos },
                        onVerTramite = { id -> /* Detalle modal en MisTramitesScreen */ },
                        onAvisos = { pantallaActual = Pantalla.Avisos },
                        onPerfil = { pantallaActual = Pantalla.Perfil },
                        onCerrarSesion = cerrarSesion
                    )
                    Pantalla.CatalogoFormatos -> CatalogoFormatosScreen(
                        onSeleccionarFormato = { formato ->
                            formatoSeleccionado = formato
                            pantallaActual = Pantalla.CrearTramite
                        },
                        onVolver = { pantallaActual = Pantalla.MisTramites }
                    )
                    Pantalla.CrearTramite -> {
                        formatoSeleccionado?.let { formato ->
                            CrearTramiteScreen(
                                usuarioId = usuarioId,
                                formato = formato,
                                onVolver = { pantallaActual = Pantalla.CatalogoFormatos },
                                onTramiteCreado = { pantallaActual = Pantalla.MisTramites }
                            )
                        } ?: run {
                            pantallaActual = Pantalla.CatalogoFormatos
                        }
                    }
                    Pantalla.BandejaNotario -> BandejaNotarioScreen(
                        usuarioId = usuarioId,
                        onCerrarSesion = cerrarSesion
                    )
                    Pantalla.Avisos -> AvisosScreen(
                        usuarioId = usuarioId,
                        onVolver = { pantallaActual = Pantalla.MisTramites },
                        onPerfil = { pantallaActual = Pantalla.Perfil },
                        onCerrarSesion = cerrarSesion
                    )
                    Pantalla.Perfil -> PerfilScreen(
                        usuarioId = usuarioId,
                        onVolver = { pantallaActual = Pantalla.MisTramites },
                        onAvisos = { pantallaActual = Pantalla.Avisos },
                        onCerrarSesion = cerrarSesion
                    )
                }
            }
        }
    }
}