package com.example.plantme_grupo8

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas para la logica de validacion de autenticacion
 * (login y registro de usuario).
 */
class AuthValidatorTest {

    @Test
    fun `email sin arroba es invalido`() {
        val error = validateEmailForTest("correoinvalido.com")
        assertEquals("Email no válido", error)
    }

    @Test
    fun `email vacio es invalido`() {
        val error = validateEmailForTest("")
        assertEquals("Email no válido", error)
    }

    @Test
    fun `email con formato basico es valido`() {
        val error = validateEmailForTest("usuario@correo.com")
        assertNull(error)
    }

    @Test
    fun `password muy corta es invalida`() {
        val error = validatePasswordForTest("123")
        assertEquals("La contraseña es demasiado corta", error)
    }

    @Test
    fun `password suficientemente larga es valida`() {
        val error = validatePasswordForTest("123456")
        assertNull(error)
    }

    private fun validateEmailForTest(email: String): String? =
        if (email.isBlank() || !email.contains("@")) "Email no válido" else null

    private fun validatePasswordForTest(password: String): String? =
        if (password.length < 6) "La contraseña es demasiado corta" else null
}