package com.example.plantme_grupo8

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas para el formato de texto que ve el usuario
 * segun los dias restantes de riego.
 */
class PlantUiFormatterTest {

    @Test
    fun `formatea correctamente cuando faltan dias positivos`() {
        val text = formatDaysRemainingForTest(5)
        assertEquals("Faltan 5 días", text)
    }

    @Test
    fun `formatea correctamente cuando toca regar hoy`() {
        val text = formatDaysRemainingForTest(0)
        assertEquals("Toca regar hoy", text)
    }

    @Test
    fun `formatea correctamente cuando la planta esta atrasada`() {
        val text = formatDaysRemainingForTest(-2)
        assertEquals("Atrasado por 2 días", text)
    }

    private fun formatDaysRemainingForTest(days: Int): String {
        return when {
            days > 0  -> "Faltan $days días"
            days == 0 -> "Toca regar hoy"
            else      -> "Atrasado por ${-days} días"
        }
    }
}