package com.example.plantme_grupo8.ui.theme.utils


object SpeciesDefault {

    // Mapa que asocia: "clave_para_backend" -> "Nombre Bonito en Pantalla"
    private val speciesMap = mapOf(
        "cactus" to "Cactus",
        "suculenta" to "Suculenta",
        "aloe" to "aloe",
        "test" to "test",
        "pothos" to "Pothos",
        "sanseviera" to "sanseviera",

    )

    /**
     * Devuelve todas las claves (ej: ["cactus", "suculenta", ...])
     * Esto es lo que necesitas para el Dropdown.
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
