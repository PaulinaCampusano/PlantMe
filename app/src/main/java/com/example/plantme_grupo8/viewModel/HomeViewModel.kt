package com.example.plantme_grupo8.viewModel
import com.example.plantme_grupo8.model.ModelPlant
// HomeViewModel.kt
// HomeViewModel.kt
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault


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

    fun addPlantAuto(
        name: String,
        speciesKey: String,
        lastWateredAtMillis: Long = System.currentTimeMillis()
    ) {
        val intervalDays = SpeciesDefault.intervalFor(speciesKey)
        val next = lastWateredAtMillis + intervalDays * DAY_MS
        val p = ModelPlant(
            id = newId(),
            name = name,
            intervalDays = intervalDays,
            nextWateringAtMillis = next,
            speciesKey = speciesKey
        )
        _plants.update { it + p }
    }


    fun deletePlant(id: Long) {
        _plants.update { list -> list.filterNot { it.id == id } }
    }
}

