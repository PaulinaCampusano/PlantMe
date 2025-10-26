package com.example.plantme_grupo8.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Home : AppRoute("home")

    // Crear/Editar planta (param opcional)
    data object PlantForm : AppRoute("plantForm?plantId={plantId}") {
        const val ARG_PLANT_ID = "plantId"
        fun createRoute(plantId: Long? = null): String =
            if (plantId == null) "plantForm" else "plantForm?plantId=$plantId"
    }

    data object Settings : AppRoute("settings")
}