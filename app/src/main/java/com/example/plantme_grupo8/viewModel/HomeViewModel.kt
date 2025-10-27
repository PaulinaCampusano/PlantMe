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
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.security.MessageDigest

//NOTIF IMPORTS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.plantme_grupo8.R
import android.Manifest






private const val DAY_MS = 24 * 60 * 60 * 1000L

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // ---- DataStore ----
    private val dataStore = application.plantsDataStore

    // (quedará sin uso directo; mantenemos por compatibilidad si lo referías en otro lugar)
    private val PLANTS_JSON = stringPreferencesKey("plants_json")

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    // -------- AUTH (multi-usuario) --------
    @Serializable
    private data class LocalUser(
        val name: String,
        val email: String,     // siempre en lowercase
        val passHash: String
    )

    private val USERS_JSON    = stringPreferencesKey("users_json")      // lista de LocalUser (JSON)
    private val CURRENT_EMAIL = stringPreferencesKey("current_email")   // email activo
    private val USER_LOGGED   = booleanPreferencesKey("user_logged")    // flag de sesión

    // ---- Legacy (para migración automática) ----
    private val LEGACY_USER_NAME      = stringPreferencesKey("user_name")
    private val LEGACY_USER_EMAIL     = stringPreferencesKey("user_email")
    private val LEGACY_USER_PASS_HASH = stringPreferencesKey("user_pass_hash")
    // ---- ------------------------ ----

    // ---- Estado en memoria (plantas) ----
    private val _plants = MutableStateFlow<List<ModelPlant>>(emptyList())
    val plants: StateFlow<List<ModelPlant>> = _plants.asStateFlow()
    // ---- ------------------------ ----

    // ---- Estado en memoria (auth) ----
    data class UserSession(val name: String, val email: String)

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    // ---- ------------------------ ----

    // ---- Canal de Notificaciones ----
    private val CHANNEL_ID = "watering_reminders"
    // ---- ------------------------ ----

    // IDs de plantas "pendientes de riego" (se notificaron y esperan que presiones el ícono de agua)
    private val _dueIds = MutableStateFlow<Set<Long>>(emptySet())
    val dueIds: StateFlow<Set<Long>> = _dueIds.asStateFlow()
    // ---- ------------------------ ----

    // Key por usuario para persistir esos "pendientes"
    private fun dueKey(): androidx.datastore.preferences.core.Preferences.Key<String> {
        val email = _session.value?.email?.lowercase() ?: "guest"
        return stringPreferencesKey("due_json_$email")
    }
    // ---- ------------------------ ----

    init {
        // Crear canal de notificaciones (una vez)
        createNotificationChannel()
        // ---- ------------------------ ----

        // --- Migración desde claves legacy a USERS_JSON (si aplica) ---
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val hasUsersList = prefs[USERS_JSON] != null
            val legacyEmail  = prefs[LEGACY_USER_EMAIL]
            val legacyName   = prefs[LEGACY_USER_NAME] ?: "Usuario"
            val legacyHash   = prefs[LEGACY_USER_PASS_HASH]

            if (!hasUsersList && legacyEmail != null && legacyHash != null) {
                val list = listOf(LocalUser(legacyName, legacyEmail.lowercase(), legacyHash))
                dataStore.edit {
                    it[USERS_JSON] = json.encodeToString(list)
                    // si estaba logueado, fijamos el current
                    if (prefs[USER_LOGGED] == true) {
                        it[CURRENT_EMAIL] = legacyEmail.lowercase()
                    }
                }
            }
        }

        // --- Sesión reactiva desde DataStore (USERS_JSON + CURRENT_EMAIL + USER_LOGGED) ---
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val logged = prefs[USER_LOGGED] ?: false
                val current = prefs[CURRENT_EMAIL]?.lowercase()
                val users = prefs[USERS_JSON]
                    ?.let { runCatching { json.decodeFromString<List<LocalUser>>(it) }.getOrNull() }
                    ?: emptyList()

                val u = users.find { it.email == current }
                _session.value = if (logged && u != null) UserSession(u.name, u.email) else null
                _isLoggedIn.value = _session.value != null
            }
        }

        // --- PLANTAS por usuario (cambia con la sesión o el DataStore) ---
        viewModelScope.launch {
            combine(session, dataStore.data) { sess, prefs ->
                val email = (sess?.email ?: "guest").lowercase()
                val key = stringPreferencesKey("plants_json_$email")
                val jsonStr = prefs[key]
                jsonStr?.let { json.decodeFromString<List<ModelPlant>>(it) } ?: emptyList()
            }.collect { loaded ->
                _plants.value = if (loaded.isEmpty()) {
                    // demo opcional para primer arranque de cada usuario
                    listOf(
                        ModelPlant(1, "Cactus", 3, System.currentTimeMillis() + 1 * DAY_MS),
                        ModelPlant(2, "Aloe",   7, System.currentTimeMillis() + 2 * DAY_MS)
                    )
                } else loaded
            }
        }
    }

    // ---------- PLANTAS ----------
    private suspend fun persist(plants: List<ModelPlant>) {
        dataStore.edit { prefs ->
            prefs[userPlantsKey()] = json.encodeToString(plants)
        }
    }

    private fun nextIdFrom(list: List<ModelPlant>): Long =
        (list.maxOfOrNull { it.id } ?: 0L) + 1

    fun addPlantAuto(
        name: String,
        speciesKey: String,
        lastWateredAtMillis: Long = System.currentTimeMillis()
    ) {
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
            persist(updated)
            _plants.value = updated

            // si 'next' ya pasó, marcar como pendiente; si no, asegurarse de que NO quede marcado
            updateDueFlag(newPlant.id, next)
        }
    }

    fun deletePlant(id: Long) {
        viewModelScope.launch {
            val updated = plants.value.filterNot { it.id == id }
            persist(updated)
            _plants.value = updated

            // Por si la planta estaba marcada vencida
            val newSet = _dueIds.value - id
            if (newSet != _dueIds.value) {
                _dueIds.value = newSet
                saveDue(newSet)
            }
        }
    }

    fun updatePlant(
        id: Long,
        name: String? = null,
        speciesKey: String? = null,
        lastWateredAtMillis: Long? = null
    ) {
        viewModelScope.launch {
            val current = plants.value
            val updated = current.map { p ->
                if (p.id != id) return@map p

                val newName = name?.trim()?.takeIf { it.isNotEmpty() } ?: p.name
                val newSpeciesKey = speciesKey ?: p.speciesKey
                val newIntervalDays =
                    speciesKey?.let { SpeciesDefault.intervalFor(it) } ?: p.intervalDays

                val previousLastWatered = p.nextWateringAtMillis - p.intervalDays * DAY_MS
                val baseLast = lastWateredAtMillis ?: previousLastWatered
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

            updateDueFlag(id, updated.first { it.id == id }.nextWateringAtMillis)
        }
    }
    // ---- ------------------------ ----

    // ---------- AUTH (multi-usuario DataStore) ----------

    /** Hash SHA-256 simple para no guardar la contraseña en texto plano */
    private fun hash(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun readUsers(): List<LocalUser> {
        val prefs = dataStore.data.first()
        val str = prefs[USERS_JSON] ?: return emptyList()
        return runCatching { json.decodeFromString<List<LocalUser>>(str) }.getOrElse { emptyList() }
    }

    private suspend fun saveUsers(list: List<LocalUser>) {
        dataStore.edit { it[USERS_JSON] = json.encodeToString(list) }
    }

    /** Registro local: agrega/actualiza usuario y deja sesión iniciada */
    fun register(name: String, email: String, password: String) {
        val n = name.trim()
        val e = email.trim().lowercase()
        val p = password.trim()
        viewModelScope.launch {
            if (n.isEmpty() || e.isEmpty() || p.isEmpty()) return@launch

            val users = readUsers()
            val newUser = LocalUser(n, e, hash(p))
            val updated = users.filterNot { it.email == e } + newUser
            saveUsers(updated)

            dataStore.edit {
                it[CURRENT_EMAIL] = e
                it[USER_LOGGED] = true
            }
            _session.value = UserSession(n, e)
            _isLoggedIn.value = true
        }
    }

    /** Inicio de sesión: busca por email y compara hash(password) */
    fun login(email: String, password: String) {
        val e = email.trim().lowercase()
        val h = hash(password)
        viewModelScope.launch {
            val users = readUsers()
            val u = users.find { it.email == e }
            val ok = (u != null && u.passHash == h)

            dataStore.edit {
                it[USER_LOGGED] = ok
                if (ok) it[CURRENT_EMAIL] = e else it.remove(CURRENT_EMAIL)
            }

            if (ok) {
                _session.value = UserSession(u!!.name, u.email)
                _isLoggedIn.value = true
            } else {
                _session.value = null
                _isLoggedIn.value = false
            }
        }
    }

    /** Cierra sesión (no borra la cuenta) */
    fun logout() {
        viewModelScope.launch {
            dataStore.edit {
                it[USER_LOGGED] = false
                it.remove(CURRENT_EMAIL)
            }
            _session.value = null
            _isLoggedIn.value = false
            // _plants se recargará automáticamente a "guest" por el combine(session, dataStore)
        }
    }

    /** Elimina por completo el usuario actual de USERS_JSON y cierra sesión */
    fun deleteLocalAccount() {
        viewModelScope.launch {
            val currentEmail = _session.value?.email?.lowercase() ?: return@launch
            val users = readUsers()
            val updated = users.filterNot { it.email == currentEmail }
            saveUsers(updated)
            dataStore.edit {
                it[USER_LOGGED] = false
                it.remove(CURRENT_EMAIL)
            }
            _session.value = null
            _isLoggedIn.value = false
            // opcional: limpiar plantas del usuario eliminado
            dataStore.edit { prefs ->
                val key = stringPreferencesKey("plants_json_$currentEmail")
                prefs.remove(key)
            }
        }
    }

    //Helper para la clave de plantas según usuario
    private fun userPlantsKey(): androidx.datastore.preferences.core.Preferences.Key<String> {
        val email = _session.value?.email?.lowercase() ?: "guest"
        return stringPreferencesKey("plants_json_$email")
    }

    //Helpers para sistema de notificaciones
    private suspend fun saveDue(set: Set<Long>) {
        dataStore.edit { it[dueKey()] = json.encodeToString(set.toList()) }
    }

    //Helper para actualizar correctamente cuando un tiempo haya vencido
    private fun updateDueFlag(id: Long, nextMillis: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newSet =
                if (nextMillis <= now) _dueIds.value + id
                else _dueIds.value - id

            if (newSet != _dueIds.value) {
                _dueIds.value = newSet
                saveDue(newSet) // ya lo tienes implementado
            }
        }
    }

    private fun markDue(id: Long) {
        viewModelScope.launch {
            val newSet = _dueIds.value + id
            _dueIds.value = newSet
            saveDue(newSet)
        }
    }

    fun waterNow(id: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = plants.value
            val updated = current.map { p ->
                if (p.id == id) {
                    val newNext = now + p.intervalDays * DAY_MS
                    p.copy(nextWateringAtMillis = newNext)
                } else p
            }
            persist(updated)
            _plants.value = updated

            val newSet = _dueIds.value - id
            _dueIds.value = newSet
            saveDue(newSet)
        }
    }

    // Sistema de notificaciones
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de riego",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Te avisa cuando una planta necesita agua."
            }
            val mgr = getApplication<Application>()
                .getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(chan)
        }
    }

    @SuppressLint("MissingPermission") // ya verificamos permiso justo arriba
    private fun sendDueNotification(plant: ModelPlant) {
        val ctx = getApplication<Application>()

        // 🔐 Android 13+: verificar permiso en runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return // sin permiso, no intentes notificar
        }

        val speciesLabel = com.example.plantme_grupo8.ui.theme.utils
            .SpeciesDefault.displayFor(plant.speciesKey ?: "") ?: "planta"

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // usa tu icono
            .setContentTitle("Necesita agua: ${plant.name}")
            .setContentText("La $speciesLabel necesita ser regada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(plant.id.toInt(), notif)
    }

    /** Escanea plantas y notifica las que ya vencieron y no fueron marcadas como pendientes */
    fun scanAndNotify() {
        val now = System.currentTimeMillis()
        val dueAlready = _dueIds.value
        plants.value.forEach { p ->
            if (p.nextWateringAtMillis <= now && p.id !in dueAlready) {
                sendDueNotification(p)
                markDue(p.id) // habilita el ícono de "agua" en UI y evita repetir notificación
            }
        }
    }
}