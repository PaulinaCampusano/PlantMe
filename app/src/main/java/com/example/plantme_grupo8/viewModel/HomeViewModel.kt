package com.example.plantme_grupo8.viewModel
import com.example.plantme_grupo8.model.ModelPlant
// HomeViewModel.kt
// HomeViewModel.kt
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



private const val DAY_MS = 24 * 60 * 60 * 1000L

class HomeViewModel : ViewModel() {

    private var nextId = 1L
    private fun newId() = nextId++

    private val _plants = MutableStateFlow(
        listOf(
            ModelPlant(newId(), "Cactus", 3, System.currentTimeMillis() + 1 * DAY_MS),
            ModelPlant(newId(), "Aloe",   7, System.currentTimeMillis() + 2 * DAY_MS)
        )
    )
    val plants = _plants.asStateFlow()

    fun addPlant(name: String, intervalDays: Int) {
        val p = ModelPlant(
            id = newId(),
            name = name,
            intervalDays = intervalDays,
            nextWateringAtMillis = System.currentTimeMillis() + intervalDays * DAY_MS
        )
        _plants.update { it + p }
    }


    fun deletePlant(id: Long) {
        _plants.update { list -> list.filterNot { it.id == id } }
    }
}

