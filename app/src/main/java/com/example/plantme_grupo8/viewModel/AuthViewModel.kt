package com.example.plantme_grupo8.viewModel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantme_grupo8.data.plantsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.security.MessageDigest

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.plantsDataStore
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    // Keys
    private val USERS_JSON    = stringPreferencesKey("users_json")
    private val CURRENT_EMAIL = stringPreferencesKey("current_email")
    private val USER_LOGGED   = booleanPreferencesKey("user_logged")

    // Legacy (migración)
    private val LEGACY_USER_NAME      = stringPreferencesKey("user_name")
    private val LEGACY_USER_EMAIL     = stringPreferencesKey("user_email")
    private val LEGACY_USER_PASS_HASH = stringPreferencesKey("user_pass_hash")

    @Serializable
    private data class LocalUser(val name: String, val email: String, val passHash: String)

    data class UserSession(val name: String, val email: String)

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Migración simple a USERS_JSON
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val hasUsers = prefs[USERS_JSON] != null
            val legacyEmail = prefs[LEGACY_USER_EMAIL]
            val legacyName  = prefs[LEGACY_USER_NAME] ?: "Usuario"
            val legacyHash  = prefs[LEGACY_USER_PASS_HASH]
            if (!hasUsers && legacyEmail != null && legacyHash != null) {
                val list = listOf(LocalUser(legacyName, legacyEmail.lowercase(), legacyHash))
                dataStore.edit {
                    it[USERS_JSON] = json.encodeToString(list)
                    if (prefs[USER_LOGGED] == true) it[CURRENT_EMAIL] = legacyEmail.lowercase()
                }
            }
        }

        // Sesión reactiva
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
    }

    private fun hash(s: String): String {
        val b = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        return b.joinToString("") { "%02x".format(it) }
    }

    private suspend fun readUsers(): List<LocalUser> {
        val str = dataStore.data.first()[USERS_JSON] ?: return emptyList()
        return runCatching { json.decodeFromString<List<LocalUser>>(str) }.getOrElse { emptyList() }
    }
    private suspend fun saveUsers(list: List<LocalUser>) {
        dataStore.edit { it[USERS_JSON] = json.encodeToString(list) }
    }

    fun register(name: String, email: String, password: String) {
        val n = name.trim(); val e = email.trim().lowercase(); val p = password.trim()
        if (n.isEmpty() || e.isEmpty() || p.isEmpty()) return
        viewModelScope.launch {
            val users = readUsers()
            val updated = users.filterNot { it.email == e } + LocalUser(n, e, hash(p))
            saveUsers(updated)
            dataStore.edit { it[CURRENT_EMAIL] = e; it[USER_LOGGED] = true }
        }
    }

    fun login(email: String, password: String) {
        val e = email.trim().lowercase(); val h = hash(password)
        viewModelScope.launch {
            val u = readUsers().find { it.email == e }
            val ok = (u != null && u.passHash == h)
            dataStore.edit {
                it[USER_LOGGED] = ok
                if (ok) it[CURRENT_EMAIL] = e else it.remove(CURRENT_EMAIL)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.edit { it[USER_LOGGED] = false; it.remove(CURRENT_EMAIL) }
        }
    }

    fun deleteLocalAccount() {
        viewModelScope.launch {
            val current = _session.value?.email?.lowercase() ?: return@launch
            val updated = readUsers().filterNot { it.email == current }
            saveUsers(updated)
            dataStore.edit { it[USER_LOGGED] = false; it.remove(CURRENT_EMAIL) }
            // (limpieza de plantas del usuario es tarea de PlantsViewModel)
        }
    }
}
