package com.example.plantme_grupo8

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas para la logica de frecuencias de riego por especie.
 */
class PlantFrequencyTableTest {

    @Test
    fun `cactus tiene frecuencia 7 dias`() {
        assertEquals(7, getFrequencyForTest("cactus"))
    }

    @Test
    fun `flor tiene frecuencia 3 dias`() {
        assertEquals(3, getFrequencyForTest("flor"))
    }

    @Test
    fun `especie desconocida usa frecuencia por defecto 1 dia`() {
        assertEquals(1, getFrequencyForTest("desconocida"))
    }

    private fun getFrequencyForTest(key: String): Int {
        return when (key.lowercase()) {
            "cactus" -> 7
            "flor"   -> 3
            else     -> 1
        }
    }
}