package com.example.plantme_grupo8.ui.theme.utils

const val DAY_MS = 24 * 60 * 60 * 1000L

fun formatRemaining(ms: Long): String {
    val s = if (ms > 0) ms / 1000 else 0
    val dias = s / 86_400
    val horas = (s % 86_400) / 3600
    val minutos = (s % 3600) / 60
    val segundos = s % 60

    val parts = buildList {
        if (dias > 0) add("$dias día${if (dias != 1L) "s" else ""}")
        if (horas > 0) add("$horas hora${if (horas != 1L) "s" else ""}")
        if (minutos > 0) add("$minutos min")
        add("$segundos s")
    }
    return parts.joinToString(", ")
}