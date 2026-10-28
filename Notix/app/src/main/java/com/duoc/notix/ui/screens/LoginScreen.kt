package com.duoc.notix.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Si ya tienes un enum con los tipos de cuenta en RegisterScreen.kt, usa ese y borra este.
enum class LoginProfile(val label: String) {
    PERSONA("Persona natural"),
    EMPRESA("Empresa"),
    NOTARIO("Notario")
}

private val NotariaGreen = Color(0xFF0F4C45)
private val NotariaGreenLight = Color(0xFFE8F1EF)
private val FieldBorder = Color(0xFFCBD5D2)
private val ErrorRed = Color(0xFFC0392B)
private val TextDark = Color(0xFF12302B)
private val TextMuted = Color(0xFF55615E)

@Composable
fun LoginScreen(
    onLoginPersona: () -> Unit,   // -> Mis trámites
    onLoginEmpresa: () -> Unit,   // -> Mis trámites (mismo flujo que persona)
    onLoginNotario: () -> Unit,   // -> Bandeja del notario
    onCreateAccount: () -> Unit   // -> Crear cuenta
) {
    var profile by remember { mutableStateOf(LoginProfile.PERSONA) }
    var email by remember { mutableStateOf("ana.demo@notariax.test") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val emailLabel = when (profile) {
        LoginProfile.PERSONA -> "Correo de prueba"
        LoginProfile.EMPRESA -> "Correo corporativo de prueba"
        LoginProfile.NOTARIO -> "Correo institucional de prueba"
    }

    fun submit() {
        error = when {
            email.isBlank() -> "Ingresa tu correo."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "El correo no tiene un formato válido."
            password.isBlank() -> "Ingresa tu contraseña."
            else -> null
        }
        if (error != null) return
        when (profile) {
            LoginProfile.PERSONA -> onLoginPersona()
            LoginProfile.EMPRESA -> onLoginEmpresa()
            LoginProfile.NOTARIO -> onLoginNotario()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF4F7F6)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Text("NotariaX", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NotariaGreen)

            Text(
                "Tus trámites notariales, siempre a la vista",
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NotariaGreen
            )
            Text(
                "Carga documentos, firma y sigue cada trámite sin ir a la notaría ni llamar por teléfono.",
                fontSize = 14.sp,
                color = TextMuted
            )

            // Aviso de entorno de demostración
            Card(
                colors = CardDefaults.cardColors(containerColor = NotariaGreenLight),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Entorno de demostración. Todos los datos son ficticios. No se conecta con ninguna notaría real.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = NotariaGreen
                )
            }

            // Selector de perfil: Persona | Empresa | Notario
            ProfileSelector(selected = profile, onSelect = { profile = it; error = null })

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(emailLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = NotariaGreen,
                    unfocusedLabelColor = TextMuted,
                    focusedBorderColor = NotariaGreen,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = NotariaGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Clave de prueba") },
                singleLine = true,
                visualTransformation =
                    if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ocultar" else "Ver", fontSize = 12.sp, color = NotariaGreen)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedLabelColor = NotariaGreen,
                    unfocusedLabelColor = TextMuted,
                    focusedBorderColor = NotariaGreen,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = NotariaGreen,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            error?.let {
                Text(it, color = ErrorRed, fontSize = 12.sp)
            }

            Button(
                onClick = ::submit,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NotariaGreen)
            ) {
                Text("Ingresar", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { submit() }, // simulación: mismo resultado que Ingresar
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, FieldBorder)
            ) {
                Text("Simular huella digital", color = NotariaGreen)
            }

            Text(
                "La verificación biométrica es solo simulación. No se usa el sensor del dispositivo.",
                fontSize = 11.sp,
                color = Color(0xFF7A8582),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", fontSize = 13.sp, color = TextMuted)
                TextButton(onClick = onCreateAccount) {
                    Text("Crear cuenta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NotariaGreen)
                }
            }
        }
    }
}

@Composable
private fun ProfileSelector(
    selected: LoginProfile,
    onSelect: (LoginProfile) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LoginProfile.entries.forEach { p ->
            val isSelected = p == selected
            Surface(
                onClick = { onSelect(p) },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) NotariaGreen else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        p.label.substringBefore(" "), // "Persona", "Empresa", "Notario"
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else NotariaGreen
                    )
                }
            }
        }
    }
}