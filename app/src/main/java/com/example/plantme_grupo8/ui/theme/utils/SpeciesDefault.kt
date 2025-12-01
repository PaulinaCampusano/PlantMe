package com.example.plantme_grupo8.ui.theme.utils


object SpeciesDefault {

    // Mapa que asocia: "clave_para_backend" -> "Nombre Bonito en Pantalla"
    private val speciesMap = mapOf(
        "cactus" to "Cactus",
        "suculenta" to "Suculenta",
        "flor" to "Flor",
        "helecho" to "Helecho",
        "monstera" to "Monstera",
        "pothos" to "Pothos",
        "orquidea" to "Orquídea",
        "bonsai" to "Bonsái"
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
