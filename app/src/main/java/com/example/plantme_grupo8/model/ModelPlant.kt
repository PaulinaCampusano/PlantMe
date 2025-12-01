package com.example.plantme_grupo8.model

//IMPORTAMOS DEPENDENCIA PARA PODER HACER EL "serializable" DEL MODELO
import kotlinx.serialization.Serializable

@Serializable
data class ModelPlant(
    val id: Long,
    val name: String,
    val intervalDays: Int,

    // Fechas calculadas
    val nextWateringAtMillis: Long,
    val lastWateringAtMillis: Long,

    val speciesKey: String? = null,

    // FALTABAN ESTOS: Campos de estado de la UI (no se guardan en BD, pero la UI los usa)
    val isMarked: Boolean = false,
    val isDue: Boolean = false
)