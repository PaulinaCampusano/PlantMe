package com.example.plantme_grupo8


import org.junit.Test
import org.junit.Assert.*

/**
 * Prueba unitaria para verificar la lógica de cálculo de plantas
 */
class PlantLogicTest {

    // Prueba 1: Sanity Check (debe pasar siempre)
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    // Prueba 2: Simula cálculo de días restantes (como en HomeScreen)
    @Test
    fun calculateDaysRemaining_isCorrect() {
        // Configuramos el próximo riego para que sea en 2 días exactos
        val now = System.currentTimeMillis()
        val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L
        val nextWatering = now + twoDaysInMillis

        val diff = nextWatering - now
        // Convertimos la diferencia de milisegundos a días
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()

        // El resultado esperado debe ser 2 días
        assertEquals(2, days)
    }

    // Prueba 3: Simula la lógica de intervalos (frecuencia de riego por especie)
    @Test
    fun speciesFrequency_isCorrect() {
        val speciesKey = "cactus"
        val expectedDays = 7

        // Asumiendo que 'cactus' necesita 7 días
        val result = getFrequency(speciesKey)
        assertEquals(expectedDays, result)
    }

    // Función auxiliar que simula la tabla de frecuencias de riego
    private fun getFrequency(key: String): Int {
        return when(key) {
            "cactus" -> 7
            "flor" -> 3
            else -> 1
        }
    }
}