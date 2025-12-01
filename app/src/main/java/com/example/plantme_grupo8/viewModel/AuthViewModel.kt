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

// Clave donde guardaremos el Token JWT
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore

    // Estado para saber si estamos cargando (para mostrar un spinner en la UI)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para saber si el usuario está logueado (Si hay token, es true)
    val isUserLogged: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            val token = preferences[JWT_TOKEN_KEY]
            !token.isNullOrEmpty() // Devuelve true si hay token
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, false)

    /**
     * LOGIN: Envía email/pass al servidor Spring Boot
     */
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val e = email.trim().lowercase()
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
                    // 2. ¡Éxito! Obtenemos el token
                    val token = response.body()!!.token
                    Log.d("AUTH", "Login exitoso. Token: $token")

                    // 3. Guardamos el token en DataStore (El celular lo recuerda)
                    dataStore.edit { preferences ->
                        preferences[JWT_TOKEN_KEY] = token
                    }
                    onSuccess()
                } else {
                    // Error 401, 404, etc.
                    Log.e("AUTH", "Error login: ${response.code()}")
                    onError("Credenciales incorrectas")
                }
            } catch (ex: Exception) {
                // Error de conexión (Servidor apagado, sin internet)
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
        val e = email.trim().lowercase()
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
                    // 2. Éxito (201 Created)
                    Log.d("AUTH", "Registro exitoso")
                    // Opcional: Podrías hacer login automático aquí si quisieras
                    onSuccess()
                } else {
                    // Error (ej: Email ya existe - 409 Conflict)
                    val errorMsg = if (response.code() == 409) "El email ya está registrado" else "Error al registrarse"
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
     * LOGOUT: Simplemente borramos el token del celular
     */
    fun logout() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(JWT_TOKEN_KEY)
            }
        }
    }

}
