package com.duoc.notix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.duoc.notix.ui.screens.CrearCuentaScreen
import com.duoc.notix.ui.screens.LoginScreen
import com.duoc.notix.ui.screens.WelcomeScreen
import com.duoc.notix.ui.theme.NotixTheme

enum class Pantalla {
    Welcome,
    Login,
    CrearCuenta
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotixTheme {
                var pantallaActual by rememberSaveable { mutableStateOf(Pantalla.Welcome) }

                when (pantallaActual) {
                    Pantalla.Welcome -> WelcomeScreen(
                        onNavigateToLogin = { pantallaActual = Pantalla.Login },
                        onNavigateToRegister = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.Login -> LoginScreen(
                        onLoginPersona = { /* TODO: Navegar a Mis trámites */ },
                        onLoginEmpresa = { /* TODO: Navegar a Mis trámites */ },
                        onLoginNotario = { /* TODO: Navegar a Bandeja del notario */ },
                        onCreateAccount = { pantallaActual = Pantalla.CrearCuenta }
                    )
                    Pantalla.CrearCuenta -> CrearCuentaScreen(
                        onVolver = { pantallaActual = Pantalla.Welcome },
                        onIngresar = { pantallaActual = Pantalla.Login }
                    )
                }
            }
        }
    }
}

