package com.example.plantme_grupo8.model

//IMPORTAMOS DEPENDENCIA PARA PODER HACER EL "serializable" DEL MODELO
import kotlinx.serialization.Serializable

@Serializable
data class ModelPlant(
    val id: Long,
    val name: String,
    val intervalDays: Int,          // el valor efectivo que usarás en Home
    val nextWateringAtMillis: Long, // siguiente riego calculado
    val speciesKey: String? = null  // ej. "cactus", "pothos" (null si fue manual)
)
