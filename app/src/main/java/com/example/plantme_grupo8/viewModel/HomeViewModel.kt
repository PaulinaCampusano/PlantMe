package com.example.plantme_grupo8.viewModel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.model.ModelPlant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.plantme_grupo8.api.RetrofitClient
import com.example.plantme_grupo8.data.plantsDataStore
import com.example.plantme_grupo8.repository.HomeRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Instanciamos el repositorio
    private val repository = HomeRepository(application.plantsDataStore)

    // Estado de la lista de plantas
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error (opcional, por si quieres mostrar un Toast/Snackbar en la UI)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refreshPlants()
    }

    fun refreshPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Limpiamos errores previos

            // Llamamos al repositorio que ahora devuelve un Result
            val result = repository.getPlants()

            // Verificamos si fue éxito o fallo
            result.onSuccess { listaDePlantas ->
                _plants.value = listaDePlantas
            }.onFailure { exception ->
                Log.e("HOME_VM", "Error al obtener plantas", exception)
                _errorMessage.value = exception.message ?: "Error desconocido"
            }

            _isLoading.value = false
        }
    }
}