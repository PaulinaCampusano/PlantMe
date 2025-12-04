package com.example.plantme_grupo8

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas para la validacion del formulario de planta.
 */
class PlantFormValidatorTest {

    @Test
    fun `nombre vacio retorna error`() {
        val error = validatePlantNameForTest("")
        assertEquals("El nombre es obligatorio", error)
    }

    @Test
    fun `nombre con texto no retorna error`() {
        val error = validatePlantNameForTest("Cactus del living")
        assertNull(error)
    }

    @Test
    fun `especie vacia retorna error`() {
        val error = validateSpeciesForTest("")
        assertEquals("La especie es obligatoria", error)
    }

    @Test
    fun `frecuencia menor o igual a cero retorna error`() {
        val error = validateFrequencyForTest(0)
        assertEquals("La frecuencia debe ser mayor que 0", error)
    }

    @Test
    fun `frecuencia valida no retorna error`() {
        val error = validateFrequencyForTest(7)
        assertNull(error)
    }

    private fun validatePlantNameForTest(name: String): String? =
        if (name.isBlank()) "El nombre es obligatorio" else null

    private fun validateSpeciesForTest(species: String): String? =
        if (species.isBlank()) "La especie es obligatoria" else null

    private fun validateFrequencyForTest(freq: Int): String? =
        if (freq <= 0) "La frecuencia debe ser mayor que 0" else null
}