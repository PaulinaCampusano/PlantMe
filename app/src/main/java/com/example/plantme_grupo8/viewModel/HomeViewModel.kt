package com.example.plantme_grupo8.viewModel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.data.plantsDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import com.example.plantme_grupo8.api.RetrofitClient
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

// Clave del token (debe coincidir)
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore

    // Estado de la lista de plantas
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Al abrir el Home, cargamos las plantas del servidor
        refreshPlants()
    }

    /**
     * Llama al servidor para actualizar la lista (Úsalo en un SwipeRefreshLayout si tienes)
     */
    fun refreshPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            loadPlantsFromServer()
            _isLoading.value = false
        }
    }

    private suspend fun getAuthHeader(): String? {
        val preferences = dataStore.data.first()
        val token = preferences[JWT_TOKEN_KEY]
        return if (token.isNullOrEmpty()) null else "Bearer $token"
    }

    private suspend fun loadPlantsFromServer() {
        val authHeader = getAuthHeader()
        if (authHeader == null) return

        try {
            val response = RetrofitClient.api.getPlants(authHeader)

            if (response.isSuccessful && response.body() != null) {
                val serverPlants = response.body()!!.map { plantRes ->
                    ModelPlant(
                        id = plantRes.id,
                        name = plantRes.nombre,
                        speciesKey = plantRes.speciesKey,
                        intervalDays = plantRes.frecuenciaDias,
                        nextWateringAtMillis = isoStringToMillis(plantRes.siguienteRiego),
                        lastWateringAtMillis = isoStringToMillis(plantRes.ultimoRiego),
                        isMarked = false,
                        isDue = false
                    )
                }

                _plants.value = serverPlants

            } else {
                Log.e("HOME_VM", "Error al cargar plantas: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("HOME_VM", "Error de conexión en Home: ${e.message}")
        }

    }

    // Utilidad de fecha (igual que en PlantsViewModel)
    private fun isoStringToMillis(isoString: String): Long {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoString)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}