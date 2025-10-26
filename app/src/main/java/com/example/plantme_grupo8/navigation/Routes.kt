package com.example.plantme_grupo8.navigation

sealed class AppRoute(val route: String) {
    data object Home    : AppRoute("home")
    data object Add     : AppRoute("add")
    data object Account : AppRoute("account")
}

// para el BottomBar (orden: Home al centro)
val bottomDestinations = listOf(
    AppRoute.Home, AppRoute.Add, AppRoute.Account
)