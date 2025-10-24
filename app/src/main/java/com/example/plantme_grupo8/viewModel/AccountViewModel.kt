package com.example.plantme_grupo8.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserPrefs(
    val pushReminders: Boolean = true,   // Notificaciones/recordatorios
    val largeText: Boolean = false,      // Texto grande (accesibilidad)
    val highContrast: Boolean = false    // Alto contraste (accesibilidad)
)

class AccountViewModel : ViewModel() {
    private val _prefs = MutableStateFlow(UserPrefs())
    val prefs: StateFlow<UserPrefs> = _prefs.asStateFlow()

    fun setPushReminders(v: Boolean) = _prefs.update { it.copy(pushReminders = v) }
    fun setLargeText(v: Boolean)     = _prefs.update { it.copy(largeText = v) }
    fun setHighContrast(v: Boolean)  = _prefs.update { it.copy(highContrast = v) }
}
