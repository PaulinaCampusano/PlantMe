package com.example.plantme_grupo8.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantme_grupo8.viewModel.AuthViewModel
import com.example.plantme_grupo8.viewModel.UiBusViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner

 // ⇠ duración mínima visible del loader
private const val MIN_LOADING_MS = 1200L
@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onGoRegister: () -> Unit,
    onLoggedIn: () -> Unit
) {
    val isLogged by vm.isLoggedIn.collectAsState()
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val owner = LocalContext.current as ViewModelStoreOwner
    val uiBus: UiBusViewModel = viewModel(owner)

    // Cuando el VM marque sesión iniciada, dejamos el loader un rato y luego navegamos
    LaunchedEffect(isLogged) {
        if (isLogged) {
            delay(MIN_LOADING_MS) // ⇠ fuerza a que se note la carga en éxito
            uiBus.hideLoading()
            onLoggedIn()
        }
    }

    // UI state
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var showPass by rememberSaveable { mutableStateOf(false) }
    var tried by rememberSaveable { mutableStateOf(false) }
    var submitting by rememberSaveable { mutableStateOf(false) }

    val screenBg = Color(0xFFF3F4F6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        HomeHeader(username = "Inicia sesión")

        Spacer(Modifier.height(16.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Bienvenido de vuelta",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    ),
                    isError = tried && !emailValid(email),
                    supportingText = {
                        if (tried && !emailValid(email)) Text("Email inválido")
                    }
                )

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    isError = tried && pass.isBlank(),
                    supportingText = {
                        if (tried && pass.isBlank()) Text("Ingresa tu contraseña")
                    }
                )

                val showBadCreds = tried && !submitting && !isLogged
                if (showBadCreds) {
                    Text(
                        "Credenciales inválidas.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        tried = true
                        if (!emailValid(email) || pass.isBlank()) return@Button
                        focus.clearFocus()

                        scope.launch {
                            submitting = true
                            uiBus.showLoading()
                            val start = System.currentTimeMillis()

                            try {
                                vm.login(email, pass)
                                // colchón por si el login es muy rápido
                                val elapsed = System.currentTimeMillis() - start
                                if (elapsed < MIN_LOADING_MS) {
                                    delay(MIN_LOADING_MS - elapsed)
                                }
                            } catch (t: Throwable) {
                                // (opcional) log/mostrar error
                            } finally {
                                uiBus.hideLoading()   // <-- SIEMPRE lo escondemos
                                submitting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !submitting
                ) {
                    Text(if (submitting) "Ingresando..." else "Ingresar")
                }

                TextButton(
                    onClick = onGoRegister,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
    var showSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(isLogged) {
        if (isLogged) {
            onLoggedIn()
            // se muestra un momento y luego navegamos (ya lo haces en LaunchedEffect)
        }
    }

    AnimatedVisibility(
        visible = showSuccess,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            "Sesión iniciada ✔",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

/* Helpers */
private fun emailValid(e: String): Boolean {
    val EMAIL_RE = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    return EMAIL_RE.matches(e.trim())
}
