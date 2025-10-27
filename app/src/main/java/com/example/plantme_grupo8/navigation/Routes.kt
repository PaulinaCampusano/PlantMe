package com.example.plantme_grupo8.navigation

sealed class AppRoute(val route: String) {

    data object Splash : AppRoute("splash")
    object Home : AppRoute("home")
    object Add : AppRoute("add")
    object Account : AppRoute("account")
    object Login : AppRoute("login")
    object Register : AppRoute("register")


}

// para el BottomBar (orden: Home al centro)
val bottomDestinations = listOf(
    AppRoute.Home, AppRoute.Add, AppRoute.Account
)