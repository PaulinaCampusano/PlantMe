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

import java.time.ZoneId
import java.time.format.DateTimeFormatter // Necesario para formatear fechas a String ISO


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
     * Esta función maneja la conversión de fechas que vienen de la API al formato Long (milisegundos)
     */
    private fun isoStringToMillis(isoString: String): Long {
        return try {
            // Caso 1: Viene con Zona (ej: 2025-12-01T10:00:00Z)
            if (isoString.contains("Z") || isoString.contains("+")) {
                java.time.ZonedDateTime.parse(isoString).toInstant().toEpochMilli()
            } else {
                // Caso 2: Viene limpia (ej: 2025-12-01T10:00:00) -> Usamos la zona del celular
                java.time.LocalDateTime.parse(isoString)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        } catch (e: Exception) {
            Log.e("DateConverter", "Error leyendo fecha: $isoString")
            // Si falla, devolvemos 'mañana' para que no salga "Atrasado" y sepas que hubo error
            System.currentTimeMillis() + 86400000L
        }
    }

    /** * Convierte Milisegundos (Long) -> Fecha ISO 8601 (String)
     * ESTA FALTABA: Convierte la fecha seleccionada en la UI al formato que espera el Backend
     */
    private fun isoStringFromMillis(millis: Long): String {
        return try {
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                // Usamos un formato estándar ISO 8601 para el Backend (sin microsegundos)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        } catch (e: Exception) {
            Log.e("DateConverter", "Error formateando fecha: $millis, ${e.message}")
            // Devuelve la fecha actual con el formato ISO si hay error
            java.time.ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }


    /** 🆕 ACTUALIZAR PLANTA (Nombre, Especie, Último Riego) */
    fun updatePlant(
        plantId: Long,
        name: String,
        speciesKey: String,
        lastWateredAtMillis: Long // Fecha de último riego seleccionada en el diálogo
    ) = viewModelScope.launch {
        // CORRECCIÓN: Usamos la nueva función auxiliar para formatear la fecha a String ISO
        val ultimoRiegoISO = isoStringFromMillis(lastWateredAtMillis)

        val authHeader = getAuthHeader() ?: return@launch

        val request = PlantRequest(
            nombre = name,
            speciesKey = speciesKey,
            ultimoRiego = ultimoRiegoISO // Usamos el String ISO formateado
        )

        try {
            // 2. Llamar al endpoint PUT
            // NOTA: Asumo que en el ApiService creaste: suspend fun updatePlant(token: String, @Path("id") id: Long, @Body request: PlantRequest): Response<PlantResponse>
            val response = RetrofitClient.api.updatePlant(authHeader, plantId, request)

            if (response.isSuccessful && response.body() != null) {
                val responseDTO = response.body()!!

                // 3. Convertir DTO a Modelo local (ModelPlant)
                val newPlant = ModelPlant(
                    id = responseDTO.id,
                    name = responseDTO.nombre,
                    // Usamos la frecuencia devuelta por el Backend
                    intervalDays = responseDTO.frecuenciaDias,
                    // Recalculamos el siguiente riego en milisegundos para la UI
                    nextWateringAtMillis = isoStringToMillis(responseDTO.siguienteRiego),
                    lastWateringAtMillis = isoStringToMillis(responseDTO.ultimoRiego),
                    speciesKey = responseDTO.speciesKey,
                    isMarked = false,
                    isDue = false
                )

                // 4. Actualizar la lista de plantas localmente
                val updatedList = _plants.value.map { plant ->
                    if (plant.id == plantId) newPlant else plant
                }
                _plants.value = updatedList // Actualizamos el StateFlow de plantas

                // Si se actualizó el riego, limpiamos la notificación pendiente
                unmarkDue(plantId)

            } else {
                Log.e("PLANTS_VM", "Error respuesta al actualizar: ${response.code()}")
            }

        } catch (e: Exception) {
            Log.e("PLANTS_VM", "Excepción al actualizar planta ${plantId}: ${e.message}")
            // Manejo de error
        }
    }

    fun deletePlant(plant: ModelPlant) {
        viewModelScope.launch {
            // 1. Obtener el token
            val authHeader = getAuthHeader()
            if (authHeader == null) return@launch

            try {
                // 2. Llamar al servidor
                // Nota: Asegúrate de que en ApiService agregaste el parámetro 'token'
                val response = RetrofitClient.api.deletePlant(authHeader, plant.id)

                if (response.isSuccessful) {
                    Log.d("PLANTS_VM", "Planta eliminada: ${plant.name}")

                    // 3. Recargar la lista para que desaparezca de la pantalla
                    loadPlantsFromServer()

                    // También limpiamos cualquier notificación pendiente de esa planta
                    unmarkDue(plant.id)
                } else {
                    Log.e("PLANTS_VM", "Error al eliminar: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PLANTS_VM", "Excepción al eliminar: ${e.message}")
            }
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