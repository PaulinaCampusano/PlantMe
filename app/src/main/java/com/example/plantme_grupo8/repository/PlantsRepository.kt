package com.example.plantme_grupo8.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.plantme_grupo8.api.PlantRequest
import com.example.plantme_grupo8.api.RetrofitClient
import com.example.plantme_grupo8.model.ModelPlant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

// Claves de DataStore
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
private val DUE_IDS_KEY = stringPreferencesKey("due_ids")

class PlantsRepository(private val dataStore: DataStore<Preferences>) {

    private val api = RetrofitClient.api

    // --- GESTIÓN DE TOKEN ---
    private suspend fun getAuthHeader(): String? {
        val preferences = dataStore.data.first()
        val token = preferences[JWT_TOKEN_KEY]
        return if (token.isNullOrEmpty()) null else "Bearer $token"
    }

    // --- GESTIÓN DE NOTIFICACIONES PENDIENTES (LOCAL) ---
    // Exponemos los IDs como un Flow para que el ViewModel los observe
    val dueIds: Flow<Set<Long>> = dataStore.data.map { preferences ->
        val json = preferences[DUE_IDS_KEY] ?: "[]"
        try {
            Json.decodeFromString<Set<Long>>(json)
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun addDueId(id: Long) {
        val currentIds = dueIds.first()
        saveDueIds(currentIds + id)
    }

    suspend fun removeDueId(id: Long) {
        val currentIds = dueIds.first()
        saveDueIds(currentIds - id)
    }

    private suspend fun saveDueIds(ids: Set<Long>) {
        dataStore.edit { preferences ->
            preferences[DUE_IDS_KEY] = Json.encodeToString(ids)
        }
    }

    // --- LLAMADAS A LA API (CRUD) ---

    // 1. OBTENER PLANTAS
    suspend fun getPlants(): Result<List<ModelPlant>> {
        val token = getAuthHeader() ?: return Result.failure(Exception("No hay sesión"))
        return try {
            val response = api.getPlants(token)
            if (response.isSuccessful && response.body() != null) {
                // Mapeamos y convertimos fechas AQUÍ en el repositorio
                val plants = response.body()!!.map { dto ->
                    ModelPlant(
                        id = dto.id,
                        name = dto.nombre,
                        speciesKey = dto.speciesKey,
                        intervalDays = dto.frecuenciaDias,
                        nextWateringAtMillis = isoStringToMillis(dto.siguienteRiego),
                        lastWateringAtMillis = isoStringToMillis(dto.ultimoRiego),
                        isMarked = false,
                        isDue = false
                    )
                }
                Result.success(plants)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. CREAR PLANTA
    suspend fun createPlant(name: String, speciesKey: String, lastWateredMillis: Long): Result<Unit> {
        val token = getAuthHeader() ?: return Result.failure(Exception("No hay sesión"))
        return try {
            val dateIso = isoStringFromMillis(lastWateredMillis)
            val request = PlantRequest(name, speciesKey, dateIso)
            val response = api.createPlant(token, request)

            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. ACTUALIZAR PLANTA
    suspend fun updatePlant(id: Long, name: String, speciesKey: String, lastWateredMillis: Long): Result<Unit> {
        val token = getAuthHeader() ?: return Result.failure(Exception("No hay sesión"))
        return try {
            val dateIso = isoStringFromMillis(lastWateredMillis)
            val request = PlantRequest(name, speciesKey, dateIso)
            val response = api.updatePlant(token, id, request)

            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. REGAR PLANTA
    suspend fun waterPlant(id: Long): Result<Unit> {
        val token = getAuthHeader() ?: return Result.failure(Exception("No hay sesión"))
        return try {
            val response = api.waterPlant(token, id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. ELIMINAR PLANTA
    suspend fun deletePlant(id: Long): Result<Unit> {
        val token = getAuthHeader() ?: return Result.failure(Exception("No hay sesión"))
        return try {
            val response = api.deletePlant(token, id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UTILIDADES DE FECHA (Privadas del Repo) ---
    private fun isoStringToMillis(isoString: String): Long {
        return try {
            if (isoString.contains("Z") || isoString.contains("+")) {
                ZonedDateTime.parse(isoString).toInstant().toEpochMilli()
            } else {
                java.time.LocalDateTime.parse(isoString)
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            }
        } catch (e: Exception) {
            System.currentTimeMillis() + 86400000L // Mañana por defecto si falla
        }
    }

    private fun isoStringFromMillis(millis: Long): String {
        return try {
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        } catch (e: Exception) {
            ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }
}