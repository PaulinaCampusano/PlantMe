package com.example.plantme_grupo8.model

data class ModelPlant(
    val id: Long,
    val name: String,
    val intervalDays: Int,
    val nextWateringAtMillis: Long
)
