package com.example.plantme_grupo8.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.data.plantsDataStore
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val DAY_MS = 24 * 60 * 60 * 1000L

class PlantsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private val CURRENT_EMAIL = stringPreferencesKey("current_email")

    // Estado
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    private val _dueIds = MutableStateFlow<Set<Long>>(emptySet())
    val dueIds: StateFlow<Set<Long>> = _dueIds.asStateFlow()

    private var currentEmail: String = "guest"

    // Notificaciones
    private val CHANNEL_ID = "watering_reminders"

    init {
        createNotificationChannel()

        // Cargar plantas y dueIds reaccionando a cambios de usuario o de preferencias
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                currentEmail = prefs[CURRENT_EMAIL]?.lowercase() ?: "guest"

                val plantsKey = stringPreferencesKey("plants_json_$currentEmail")
                val plantsStr = prefs[plantsKey]
                _plants.value = plantsStr
                    ?.let { runCatching { json.decodeFromString<List<ModelPlant>>(it) }.getOrNull() }
                    ?: emptyList()

                val dueKey = stringPreferencesKey("due_json_$currentEmail")
                val dueStr = prefs[dueKey]
                _dueIds.value = dueStr
                    ?.let { runCatching { json.decodeFromString<List<Long>>(it) }.getOrNull() }
                    ?.toSet() ?: emptySet()
            }
        }
    }

    // ---------- Persistencia ----------
    private fun userPlantsKey() = stringPreferencesKey("plants_json_$currentEmail")
    private fun dueKey()        = stringPreferencesKey("due_json_$currentEmail")

    private suspend fun persist(plants: List<ModelPlant>) {
        dataStore.edit { it[userPlantsKey()] = json.encodeToString(plants) }
    }
    private suspend fun saveDue(set: Set<Long>) {
        dataStore.edit { it[dueKey()] = json.encodeToString(set.toList()) }
    }

    // ---------- CRUD ----------
    private fun nextIdFrom(list: List<ModelPlant>): Long =
        (list.maxOfOrNull { it.id } ?: 0L) + 1

    fun addPlantAuto(name: String, speciesKey: String, lastWateredAtMillis: Long = System.currentTimeMillis()) {
        val intervalDays = SpeciesDefault.intervalFor(speciesKey)
        val next = lastWateredAtMillis + intervalDays * DAY_MS
        viewModelScope.launch {
            val current = plants.value
            val newPlant = ModelPlant(
                id = nextIdFrom(current),
                name = name.trim(),
                intervalDays = intervalDays,
                nextWateringAtMillis = next,
                speciesKey = speciesKey
            )
            val updated = current + newPlant
            persist(updated); _plants.value = updated
            updateDueFlag(newPlant.id, next)
        }
    }

    fun deletePlant(id: Long) {
        viewModelScope.launch {
            val updated = plants.value.filterNot { it.id == id }
            persist(updated); _plants.value = updated
            val newSet = _dueIds.value - id
            if (newSet != _dueIds.value) { _dueIds.value = newSet; saveDue(newSet) }
        }
    }

    fun updatePlant(id: Long, name: String? = null, speciesKey: String? = null, lastWateredAtMillis: Long? = null) {
        viewModelScope.launch {
            val current = plants.value
            var newNextForId: Long? = null
            val updated = current.map { p ->
                if (p.id != id) return@map p
                val newName = name?.trim()?.takeIf { it.isNotEmpty() } ?: p.name
                val newSpeciesKey = speciesKey ?: p.speciesKey
                val newIntervalDays = speciesKey?.let { SpeciesDefault.intervalFor(it) } ?: p.intervalDays
                val previousLastWatered = p.nextWateringAtMillis - p.intervalDays * DAY_MS
                val baseLast = lastWateredAtMillis ?: previousLastWatered
                val newNext = baseLast + newIntervalDays * DAY_MS
                newNextForId = newNext
                p.copy(
                    name = newName,
                    speciesKey = newSpeciesKey,
                    intervalDays = newIntervalDays,
                    nextWateringAtMillis = newNext
                )
            }
            persist(updated); _plants.value = updated
            newNextForId?.let { updateDueFlag(id, it) }
        }
    }

    // ---------- Due / Agua ----------
    private fun updateDueFlag(id: Long, nextMillis: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newSet = if (nextMillis <= now) _dueIds.value + id else _dueIds.value - id
            if (newSet != _dueIds.value) { _dueIds.value = newSet; saveDue(newSet) }
        }
    }

    private fun markDue(id: Long) {
        viewModelScope.launch {
            val set = _dueIds.value + id
            _dueIds.value = set; saveDue(set)
        }
    }

    fun waterNow(id: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updated = plants.value.map { p ->
                if (p.id == id) p.copy(nextWateringAtMillis = now + p.intervalDays * DAY_MS) else p
            }
            persist(updated); _plants.value = updated
            val set = _dueIds.value - id
            _dueIds.value = set; saveDue(set)
        }
    }

    // ---------- Notificaciones ----------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID, "Recordatorios de riego", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Te avisa cuando una planta necesita agua." }
            val mgr = getApplication<Application>().getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(chan)
        }
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

    /** Llama esto periódicamente desde la UI */
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
}
