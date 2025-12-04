package com.example.plantme_grupo8.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.data.plantsDataStore
import com.example.plantme_grupo8.repository.AuthRepository // ¡Importante!
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Instancia del Repository: El ViewModel solo interactúa con él.
    private val repository = AuthRepository(application.plantsDataStore)

    // --- ESTADOS (StateFlow) ---
    // 1. Estado de carga (Spinner)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 2. Estado de Login (True si hay token)
    private val _isLoggedInternal = MutableStateFlow(false)
    val isUserLoggedFlow: StateFlow<Boolean> = _isLoggedInternal.asStateFlow()

    // 3. Estado del Nombre de Usuario (Para el "Hola, Usuario")
    private val _usernameFlow = MutableStateFlow("Mi Jardín")
    val usernameFlow: StateFlow<String> = _usernameFlow.asStateFlow()

    init {
        // Observamos el Flow del Repository, que a su vez lee de DataStore.
        // Esto garantiza que el estado de la UI se actualice automáticamente
        // si el token o el nombre cambian (ej: después de login o logout).
        viewModelScope.launch {
            repository.sessionData.collect { (token, name) ->
                _isLoggedInternal.value = !token.isNullOrEmpty()
                _usernameFlow.value = name ?: "Mi Jardín"
            }
        }
    }

    /**
     * LOGIN: Delega la tarea al Repository y maneja el resultado (éxito/error).
     */
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val e = email.trim()
        val p = password.trim()
        if (e.isEmpty() || p.isEmpty()) {
            onError("Completa los campos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Llamada al Repository
            val result = repository.login(e, p)

            _isLoading.value = false

            // 2. Manejo de resultado
            result.onSuccess {
                onSuccess() // Notifica a la UI que la navegación puede continuar
            }.onFailure { exception ->
                // Muestra mensaje de error del Repository
                onError(exception.message ?: "Error desconocido")
            }
        }
    }

    /**
     * REGISTRO: Delega la tarea al Repository y maneja el resultado (éxito/error).
     */
    fun register(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val n = name.trim(); val e = email.trim(); val p = password.trim()
        if (n.isEmpty() || e.isEmpty() || p.isEmpty()) {
            onError("Completa los campos")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            // 1. Llamada al Repository
            val result = repository.register(n, e, p)

            _isLoading.value = false

            // 2. Manejo de resultado
            result.onSuccess {
                onSuccess()
            }.onFailure { exception ->
                // Muestra mensaje de error del Repository
                onError(exception.message ?: "Error al registrarse")
            }
        }
    }

    /**
     * LOGOUT: Delega la tarea al Repository.
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            // El `init` se encarga de que la UI se actualice automáticamente.
        }
    }
}