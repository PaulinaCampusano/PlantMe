package com.example.plantme_grupo8.repository


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.plantme_grupo8.api.LoginRequest
import com.example.plantme_grupo8.api.RegisterRequest
import com.example.plantme_grupo8.api.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Definimos las claves aquí, ya que el Repository es quien las gestiona
private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
private val USER_NAME_KEY = stringPreferencesKey("user_name_display")

class AuthRepository(private val dataStore: DataStore<Preferences>) {

    private val api = RetrofitClient.api

    // 1. Exponer datos de sesión como un Flow (para que el ViewModel los observe)
    // Devuelve un par: (Token?, NombreUsuario?)
    val sessionData: Flow<Pair<String?, String?>> = dataStore.data.map { preferences ->
        val token = preferences[JWT_TOKEN_KEY]
        val name = preferences[USER_NAME_KEY]
        Pair(token, name)
    }

    // 2. Función de Login
    // Devuelve true si fue exitoso, false si falló, o lanza excepción
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                val displayName = email.substringBefore("@") // Lógica de nombre

                // Guardar en DataStore
                dataStore.edit { preferences ->
                    preferences[JWT_TOKEN_KEY] = token
                    preferences[USER_NAME_KEY] = displayName
                }
                Result.success(displayName)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Función de Registro
    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val response = api.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val msg = if (response.code() == 409) "Email ya registrado" else "Error ${response.code()}"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. Función de Logout
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
        }
    }
}