package com.example.plantme_grupo8.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.data.plantsDataStore
import com.example.plantme_grupo8.api.LoginRequest
import com.example.plantme_grupo8.api.RegisterRequest
import com.example.plantme_grupo8.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


// Claves de DataStore
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
private val USER_NAME_KEY = stringPreferencesKey("user_name_display") // Para guardar el nombre/email

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore

    // --- ESTADOS ---

    // 1. Estado de carga (Spinner)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 2. Estado de Login (True si hay token)
    private val _isLoggedInternal = MutableStateFlow(false)
    // Exponemos esta para que AppNavHost la lea.
    // NOTA: Si en AppNavHost usas "isUserLogged", cambia el nombre aquí o allá.
    // Para compatibilidad con tu código anterior, la llamaremos isUserLoggedFlow
    val isUserLoggedFlow: StateFlow<Boolean> = _isLoggedInternal.asStateFlow()

    // 3. Estado del Nombre de Usuario (Para el "Hola, Usuario")
    private val _usernameFlow = MutableStateFlow("Mi Jardín")
    val usernameFlow: StateFlow<String> = _usernameFlow.asStateFlow()

    init {
        // Al iniciar la App, verificamos si ya hay sesión guardada y recuperamos el nombre
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val token = preferences[JWT_TOKEN_KEY]
                val savedName = preferences[USER_NAME_KEY]

                // Actualizamos estado de login
                _isLoggedInternal.value = !token.isNullOrEmpty()

                // Actualizamos nombre si existe, sino "Mi Jardín"
                if (!savedName.isNullOrEmpty()) {
                    _usernameFlow.value = savedName
                } else {
                    _usernameFlow.value = "usuario"
                }
            }
        }
    }

    /**
     * LOGIN: Envía email/pass al servidor Spring Boot
     */
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val e = email.trim() // Quitamos lowercase forzado por si acaso
        val p = password.trim()

        if (e.isEmpty() || p.isEmpty()) {
            onError("Por favor completa los campos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Llamada a la API
                val response = RetrofitClient.api.login(LoginRequest(e, p))

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    Log.d("AUTH", "Login exitoso. Token recibido.")

                    // 2. Extraer nombre para mostrar (Usamos la parte antes del @ del email)
                    val displayName = e.substringBefore("@")

                    // 3. Guardar Token y Nombre en DataStore
                    dataStore.edit { preferences ->
                        preferences[JWT_TOKEN_KEY] = token
                        preferences[USER_NAME_KEY] = displayName
                    }

                    // 4. IMPORTANTE: Actualizar estados locales inmediatamente para la UI
                    _usernameFlow.value = displayName
                    _isLoggedInternal.value = true

                    onSuccess()
                } else {
                    Log.e("AUTH", "Error login: ${response.code()}")
                    onError("Credenciales incorrectas")
                }
            } catch (ex: Exception) {
                Log.e("AUTH", "Excepción login: ${ex.message}")
                onError("Error de conexión con el servidor")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * REGISTRO: Crea usuario en Spring Boot
     */
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val n = name.trim()
        val e = email.trim()
        val p = password.trim()

        if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {
            onError("Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Llamada a la API de Registro
                val response = RetrofitClient.api.register(RegisterRequest(n, e, p))

                if (response.isSuccessful) {
                    Log.d("AUTH", "Registro exitoso")

                    // Opcional: Podrías hacer login automático aquí o guardar el nombre
                    // Por ahora, solo notificamos éxito
                    onSuccess()
                } else {
                    val errorMsg = if (response.code() == 409) "El email ya está registrado" else "Error al registrarse (${response.code()})"
                    onError(errorMsg)
                }
            } catch (ex: Exception) {
                onError("Error de conexión: ${ex.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * LOGOUT: Borramos token y datos locales
     */
    fun logout() {
        viewModelScope.launch {
            // Borrar de DataStore
            dataStore.edit { preferences ->
                preferences.remove(JWT_TOKEN_KEY)
                preferences.remove(USER_NAME_KEY)
            }
            // Resetear estados en memoria
            _isLoggedInternal.value = false
            _usernameFlow.value = "Mi Jardín"
        }
    }
}
