package com.example.plantme_grupo8.ui.theme.utils

object SpeciesDefault {

    // Mapa que asocia: "clave_para_backend" -> "Nombre Bonito en Pantalla"
    private val speciesMap = mapOf(
        "cactus" to "Cactus",
        "suculenta" to "suculenta",
        "aloe" to "aloe",
        "test" to "test",
        "pothos" to "pothos",
        "sansevieria" to "sansevieria",

        )

    /**
     * Devuelve el mapa completo de especies (clave -> nombre visible).
     * Esta función es necesaria para la iteración clave-valor en la UI (HomeScreen).
     */
    fun getAllSpecies(): Map<String, String> {
        return speciesMap
    }

    /**
     * Devuelve todas las claves (ej: ["cactus", "suculenta", ...])
     */
    fun getAllKeys(): List<String> {
        return speciesMap.keys.toList().sorted()
    }

    /**
     * Dado una clave ("cactus"), devuelve el nombre bonito ("Cactus").
     */
    fun displayFor(key: String): String? {
        return speciesMap[key]
    }
}