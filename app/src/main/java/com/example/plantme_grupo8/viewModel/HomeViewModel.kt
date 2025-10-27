package com.example.plantme_grupo8.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
//DATASTORE DE "ModelPlant"
import com.example.plantme_grupo8.data.plantsDataStore

//IMPORTS PARA USO DE DATASTORE
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
//COROUTINES PARA CAMBIOS EN SEGUNDO PLANO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
//IMPORT PARA Serializations
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

import androidx.lifecycle.ViewModel

private const val DAY_MS = 24 * 60 * 60 * 1000L

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // ---- DataStore ----
    private val dataStore = application.plantsDataStore
    private val PLANTS_JSON = stringPreferencesKey("plants_json")
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    // ---- Estado en memoria ----
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    init {
        // Cargar desde DataStore al iniciar
        viewModelScope.launch {
            dataStore.data
                .map { prefs ->
                    prefs[PLANTS_JSON]
                        ?.let { json.decodeFromString<List<ModelPlant>>(it) }
                    // lista demo si no hay nada guardado aún (puedes dejarla vacía si prefieres)
                        ?: listOf(
                            ModelPlant(1, "Cactus", 3, System.currentTimeMillis() + 1 * DAY_MS),
                            ModelPlant(2, "Aloe",   7, System.currentTimeMillis() + 2 * DAY_MS)
                        )
                }
                .collect { loaded ->
                    _plants.value = loaded
                }
        }
    }

    //FUNCION PARA DECODIFICAR JSON
    private suspend fun persist(plants: List<ModelPlant>) {
        dataStore.edit { prefs ->
            prefs[PLANTS_JSON] = json.encodeToString(plants)
        }
    }

    //FUNCION PARA CONTROL EN EL ORDEN DE LA LISTA
    private fun nextIdFrom(list: List<ModelPlant>): Long =
        (list.maxOfOrNull { it.id } ?: 0L) + 1


    //FUNCION PARA AÑADIR
    fun addPlantAuto(
        name: String,
        speciesKey: String,
        lastWateredAtMillis: Long = System.currentTimeMillis()
    ) {
        val intervalDays = SpeciesDefault.intervalFor(speciesKey)
        val next = lastWateredAtMillis + intervalDays * DAY_MS
        viewModelScope.launch {
            // lee el snapshot actual de memoria (que ya refleja DataStore)
            val current = plants.value
            val newPlant = ModelPlant(
                id = nextIdFrom(current),
                name = name.trim(),
                intervalDays = intervalDays,
                nextWateringAtMillis = next,
                speciesKey = speciesKey
            )
            val updated = current + newPlant
            // 1) persiste
            persist(updated)
            // 2) y actualiza estado
            _plants.value = updated
        }
    }

    //ELIMINAR DE LA LISTA
    fun deletePlant(id: Long) {
        viewModelScope.launch {
            val updated = plants.value.filterNot { it.id == id }
            persist(updated)
            _plants.value = updated
        }
    }

    // ACTUALIZAR ITEM (nombre, especie y/o "último riego")
    fun updatePlant(
        id: Long,
        name: String? = null,                 // si es null, se mantiene
        speciesKey: String? = null,           // si es null, se mantiene
        lastWateredAtMillis: Long? = null     // si es null, se mantiene el cálculo actual
    ) {
        viewModelScope.launch {
            val current = plants.value

            val updated = current.map { p ->
                if (p.id != id) return@map p

                // Nombre
                val newName = name?.trim()?.takeIf { it.isNotEmpty() } ?: p.name

                // Especie e intervalo
                val newSpeciesKey = speciesKey ?: p.speciesKey
                val newIntervalDays = speciesKey?.let { SpeciesDefault.intervalFor(it) } ?: p.intervalDays

                // Reconstruimos "último riego" a partir del estado que teníamos:
                // lastSaved = next - (intervalo anterior en ms)
                val previousLastWatered = p.nextWateringAtMillis - p.intervalDays * DAY_MS

                // Si nos pasaron un nuevo "último riego", usamos ese. Sino, mantenemos el anterior
                val baseLast = lastWateredAtMillis ?: previousLastWatered

                // Con el "ultimo riego" (baseLast) y el (nuevo) intervalo, calculamos el próximo
                val newNext = baseLast + newIntervalDays * DAY_MS

                p.copy(
                    name = newName,
                    speciesKey = newSpeciesKey,
                    intervalDays = newIntervalDays,
                    nextWateringAtMillis = newNext
                )
            }

            persist(updated)
            _plants.value = updated
        }
    }

}


