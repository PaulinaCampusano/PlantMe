package com.example.plantme_grupo8.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.data.plantsDataStore
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.repository.PlantsRepository // Importamos el Repo
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantsViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Instanciamos el Repository
    private val repository = PlantsRepository(application.plantsDataStore)

    // Constantes de Notificación
    private val CHANNEL_ID = "channel_riego"
    private val NOTIF_ID = 101

    // --- ESTADOS ---
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    // Observamos los dueIds directamente del Repository
    private val _dueIds = MutableStateFlow(emptySet<Long>())
    val dueIds: StateFlow<Set<Long>> = _dueIds.asStateFlow()

    init {
        createNotificationChannel()
        loadPlantsFromServer()

        // Sincronizamos los dueIds del repo con el estado local
        viewModelScope.launch {
            repository.dueIds.collect { ids ->
                _dueIds.value = ids
            }
        }
    }

    // --- FUNCIONES QUE LLAMAN AL REPOSITORIO ---

    fun loadPlantsFromServer() {
        viewModelScope.launch {
            repository.getPlants()
                .onSuccess { lista -> _plants.value = lista }
                .onFailure { e -> Log.e("PLANTS_VM", "Error cargar: ${e.message}") }
        }
    }


    // VERSIÓN CORRECTA PARA LA UI (Recibe Long y llama al repo)
    fun createPlant(nombre: String, speciesKey: String, ultimoRiegoMillis: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.createPlant(nombre, speciesKey, ultimoRiegoMillis)
                .onSuccess {
                    loadPlantsFromServer()
                    onSuccess()
                }
                .onFailure { e -> onError(e.message ?: "Error desconocido") }
        }
    }

    // SOBRECARGA para compatibilidad si tu UI envía String (AddPlantScreen antigua)
    // Intenta usar la de arriba modificando AddPlantScreen si puedes.
    fun createPlant(nombre: String, speciesKey: String, ultimoRiegoISO: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Convertimos el String ISO a Long para pasarlo al Repo
        val millis = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                java.time.LocalDateTime.parse(ultimoRiegoISO).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else 0L
        } catch (e: Exception) { System.currentTimeMillis() }

        createPlant(nombre, speciesKey, millis, onSuccess, onError)
    }

    fun updatePlant(id: Long, name: String, speciesKey: String, lastWateredMillis: Long) {
        viewModelScope.launch {
            repository.updatePlant(id, name, speciesKey, lastWateredMillis)
                .onSuccess {
                    loadPlantsFromServer()
                    repository.removeDueId(id) // Limpiar notificación si se actualizó
                }
                .onFailure { Log.e("PLANTS_VM", "Error update: ${it.message}") }
        }
    }

    fun deletePlant(plant: ModelPlant) {
        viewModelScope.launch {
            repository.deletePlant(plant.id)
                .onSuccess {
                    loadPlantsFromServer()
                    repository.removeDueId(plant.id)
                }
                .onFailure { Log.e("PLANTS_VM", "Error delete: ${it.message}") }
        }
    }

    fun waterPlant(plant: ModelPlant) {
        viewModelScope.launch {
            repository.waterPlant(plant.id)
                .onSuccess {
                    loadPlantsFromServer()
                    repository.removeDueId(plant.id)
                }
                .onFailure { Log.e("PLANTS_VM", "Error water: ${it.message}") }
        }
    }

    // --- NOTIFICACIONES (Lógica de UI) ---

    fun scanAndNotify() {
        val now = System.currentTimeMillis()
        val due = _dueIds.value
        plants.value.forEach { p ->
            if (p.nextWateringAtMillis <= now && p.id !in due) {
                sendDueNotification(p)
                markDue(p.id)
            }
        }
    }

    private fun markDue(id: Long) {
        viewModelScope.launch { repository.addDueId(id) }
    }

    @SuppressLint("MissingPermission")
    private fun sendDueNotification(plant: ModelPlant) {
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val label = SpeciesDefault.displayFor(plant.speciesKey ?: "") ?: "planta"
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Necesita agua: ${plant.name}")
            .setContentText("La $label necesita ser regada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(plant.id.toInt(), notif)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(CHANNEL_ID, "Riego", NotificationManager.IMPORTANCE_DEFAULT)
            val mgr = getApplication<Application>().getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(chan)
        }
    }
}
