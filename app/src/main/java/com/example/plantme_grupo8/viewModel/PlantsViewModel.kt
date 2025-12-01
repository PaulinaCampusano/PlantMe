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
import com.example.plantme_grupo8.api.PlantRequest
import com.example.plantme_grupo8.api.RetrofitClient
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import java.time.Instant
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.data.plantsDataStore
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

// Constantes globales
private const val DAY_MS = 24 * 60 * 60 * 1000L

// Claves de DataStore
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
private val DUE_IDS_KEY = stringPreferencesKey("due_ids") // Si quisieras persistir notificaciones

class PlantsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore

    // Constantes de Notificación
    private val CHANNEL_ID = "channel_riego"
    private val NOTIF_ID = 101

    // --- ESTADOS (StateFlow) ---

    // Lista de plantas (viene del Backend)
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()

    // IDs de plantas que ya necesitan riego (Estado local para no repetir notificaciones)
    private val _dueIds = MutableStateFlow(emptySet<Long>())
    val dueIds: StateFlow<Set<Long>> = _dueIds.asStateFlow()

    init {
        createNotificationChannel()
        // Cargar plantas del servidor al iniciar la ViewModel
        loadPlantsFromServer()
    }

    // =========================================================================
    //              FUNCIONES DE RED (BACKEND)
    // =========================================================================

    /**
     * Obtiene el token JWT guardado y le agrega el prefijo "Bearer ".
     */
    private suspend fun getAuthHeader(): String? {
        val preferences = dataStore.data.first()
        val token = preferences[JWT_TOKEN_KEY]
        return if (token.isNullOrEmpty()) null else "Bearer $token"
    }

    /**
     * Carga la lista de plantas desde Spring Boot (GET /api/plantas)
     */
    fun loadPlantsFromServer() {
        viewModelScope.launch {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                Log.e("PLANTS_VM", "No hay token, no se pueden cargar plantas.")
                return@launch
            }

            try {
                val response = RetrofitClient.api.getPlants(authHeader)

                if (response.isSuccessful && response.body() != null) {
                    // Mapeamos la respuesta del servidor a nuestro modelo local
                    val serverPlants = response.body()!!.map { plantRes ->
                        ModelPlant(
                            id = plantRes.id,
                            name = plantRes.nombre,
                            speciesKey = plantRes.speciesKey,

                            // 1. AÑADIDO: Mapeamos la frecuencia (intervalo)
                            intervalDays = plantRes.frecuenciaDias,

                            // 2. Fechas convertidas
                            nextWateringAtMillis = isoStringToMillis(plantRes.siguienteRiego),
                            lastWateringAtMillis = isoStringToMillis(plantRes.ultimoRiego),

                            // 3. Valores por defecto para la UI
                            isMarked = false,
                            isDue = false
                        )
                    }
                    _plants.value = serverPlants
                    Log.d("PLANTS_VM", "Plantas cargadas: ${serverPlants.size}")
                } else {
                    Log.e("PLANTS_VM", "Error cargando plantas: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PLANTS_VM", "Excepción al cargar plantas: ${e.message}")
            }
        }
    }

    /**
     * Crea una planta en Spring Boot (POST /api/plantas)
     */
    fun createPlant(nombre: String, speciesKey: String, ultimoRiego: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val authHeader = getAuthHeader()
            if (authHeader == null) {
                onError("Sesión expirada. Reinicia la app.")
                return@launch
            }

            try {
                val request = PlantRequest(nombre, speciesKey, ultimoRiego)
                val response = RetrofitClient.api.createPlant(authHeader, request)

                if (response.isSuccessful) {
                    // Si se creó bien, recargamos la lista completa del servidor
                    loadPlantsFromServer()
                    onSuccess()
                } else {
                    onError("Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Error de conexión.")
                Log.e("PLANTS_VM", "Error createPlant: ${e.message}")
            }
        }
    }

    /**
     * Eliminar planta (NOTA: Necesitas implementar DELETE en el backend para que esto funcione realmente)
     * Por ahora, solo simula o podrías implementar el endpoint después.
     */
    fun deletePlant(plant: ModelPlant) {
        // TODO: Implementar endpoint DELETE /api/plantas/{id} en Spring Boot
        // Por ahora no hace nada para no crashear
        Log.w("PLANTS_VM", "Borrar planta no implementado en backend aun.")
    }

    /**
     * Regar planta (PUT /api/plantas/{id}/regar)
     * Como implementamos esto en el backend, ¡podemos usarlo!
     */
    fun waterPlant(plant: ModelPlant) {
        viewModelScope.launch {
            val authHeader = getAuthHeader() ?: return@launch

            try {
                // LLAMADA AL SERVIDOR
                val response = RetrofitClient.api.waterPlant(authHeader, plant.id)

                if (response.isSuccessful) {
                    Log.d("PLANTS_VM", "¡Planta regada!")
                    // RECARGAMOS LA LISTA para ver la nueva fecha
                    loadPlantsFromServer()
                }
            } catch (e: Exception) {
                Log.e("PLANTS_VM", "Error al regar: ${e.message}")
            }
        }
    }

    // =========================================================================
    //              UTILIDADES (FECHAS Y NOTIFICACIONES)
    // =========================================================================

    /**
     * Convierte fecha ISO 8601 (String) -> Milisegundos (Long)
     */
    private fun isoStringToMillis(isoString: String): Long {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoString)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("DateConverter", "Error parseando fecha: $isoString")
            System.currentTimeMillis() // Fallback a 'ahora' si falla
        }
    }

    // --- LÓGICA DE NOTIFICACIONES RECUPERADA ---

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

        // Verificación de permiso para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val label = SpeciesDefault.displayFor(plant.speciesKey ?: "") ?: "planta"

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Asegúrate de tener este icono
            .setContentTitle("Necesita agua: ${plant.name}")
            .setContentText("La $label necesita ser regada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(plant.id.toInt(), notif)
    }

    /**
     * Escanea las plantas y lanza notificación si nextWatering <= now
     */
    fun scanAndNotify() {
        val now = System.currentTimeMillis()
        val due = _dueIds.value
        plants.value.forEach { p ->
            // Si ya pasó la fecha de riego y NO hemos notificado aún
            if (p.nextWateringAtMillis <= now && p.id !in due) {
                sendDueNotification(p)
                markDue(p.id)
            }
        }
    }

    // --- MANEJO DE ESTADO LOCAL DE NOTIFICACIONES ---

    fun markDue(id: Long) {
        _dueIds.update { it + id }
    }

    fun unmarkDue(id: Long) {
        _dueIds.update { it - id }
    }
}