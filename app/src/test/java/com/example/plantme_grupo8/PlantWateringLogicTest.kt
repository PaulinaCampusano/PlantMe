package com.example.plantme_grupo8

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para la lógica de cálculo de riego
 * usando milisegundos.
 */
class PlantWateringLogicTest {

    private val oneDayMillis = 24 * 60 * 60 * 1000L

    @Test
    fun `calcula correctamente dias restantes cuando faltan 2 dias`() {
        val now = 1_000_000_000_000L
        val nextWatering = now + 2 * oneDayMillis

        val diff = nextWatering - now
        val days = (diff / oneDayMillis).toInt()

        assertEquals(2, days)
    }

    @Test
    fun `dias negativos indican riego atrasado`() {
        val now = 1_000_000_000_000L
        val shouldHaveBeenWatered = now - 3 * oneDayMillis

        val diff = shouldHaveBeenWatered - now
        val days = (diff / oneDayMillis).toInt()

        assertEquals(-3, days)
    }

    @Test
    fun `cambiar la frecuencia modifica los dias restantes`() {
        val now = 1_000_000_000_000L
        val lastWatering = now - oneDayMillis  // se regó ayer

        val oldFrequencyDays = 2
        val newFrequencyDays = 5

        val nextOld = lastWatering + oldFrequencyDays * oneDayMillis
        val nextNew = lastWatering + newFrequencyDays * oneDayMillis

        val remainingOld = ((nextOld - now) / oneDayMillis).toInt()
        val remainingNew = ((nextNew - now) / oneDayMillis).toInt()

        assertEquals(1, remainingOld)
        assertEquals(4, remainingNew)
    }
}