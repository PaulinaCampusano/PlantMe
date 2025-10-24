package com.example.plantme_grupo8.ui.theme.utils


data class Species(val key: String, val display: String, val intervalDays: Int)

object SpeciesDefault {
    val list = listOf(
        Species("cactus",       "Cactus",       30),
        Species("pothos",       "Pothos",        7),
        Species("aloe",         "Aloe",         14),
        Species("sansevieria",  "Sansevieria",  18),
        Species("suculenta",    "Suculenta",    21)
    )

    fun intervalFor(key: String): Int =
        list.firstOrNull { it.key == key }?.intervalDays ?: 7

    fun displayFor(key: String): String =
        list.firstOrNull { it.key == key }?.display ?: key
}