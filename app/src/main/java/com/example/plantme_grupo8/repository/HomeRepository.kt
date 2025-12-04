package com.example.plantme_grupo8.repository
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
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
import kotlinx.coroutines.flow.first
import java.time.ZonedDateTime

// Definimos la clave fuera de la clase, igual que en AuthRepository
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")

class HomeRepository(private val dataStore: DataStore<Preferences>) {

    // Instancia de la API
    private val api = RetrofitClient.api

    /**
     * Obtiene las plantas del servidor.
     * Devuelve un Result con la lista de plantas (Success) o una excepción (Failure).
     */
    suspend fun getPlants(): Result<List<ModelPlant>> {
        val authHeader = getAuthHeader()
        if (authHeader == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }

        return try {
            val response = api.getPlants(authHeader)

            if (response.isSuccessful && response.body() != null) {
                // Mapeo de datos: De la respuesta del servidor al modelo de la App
                val plantsList = response.body()!!.map { plantRes ->
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
                Result.success(plantsList)
            } else {
                val errorMsg = "Error al cargar plantas: ${response.code()}"
                Log.e("HOME_REPO", errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("HOME_REPO", "Error de conexión: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun getAuthHeader(): String? {
        // Usamos .first() porque solo necesitamos el token una vez para la llamada
        val preferences = dataStore.data.first()
        val token = preferences[JWT_TOKEN_KEY]
        return if (token.isNullOrEmpty()) null else "Bearer $token"
    }

    // Utilidad para convertir fechas
    private fun isoStringToMillis(isoString: String): Long {
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoString)
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
}