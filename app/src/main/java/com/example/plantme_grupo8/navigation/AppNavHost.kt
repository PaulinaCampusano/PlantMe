package com.example.plantme_grupo8.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantme_grupo8.ui.screens.auth.LoginScreen
import com.example.plantme_grupo8.ui.screens.auth.RegisterScreen
import com.example.plantme_grupo8.ui.screens.plant.PlantFormScreen
import com.example.plantme_grupo8.ui.screens.settings.SettingsScreen
import com.example.plantme_grupo8.ui.wrappers.HomeRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = AppRoute.Login.route
) {
    val navController = rememberNavController()
    val slide = 250

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Animaciones simples y estables (aporta a IL2.2)
        enterTransition = {
            slideInHorizontally(animationSpec = tween(slide)) { fullWidth -> fullWidth }
        },
        exitTransition = {
            slideOutHorizontally(animationSpec = tween(slide)) { fullWidth -> -fullWidth }
        },
        popEnterTransition = {
            slideInHorizontally(animationSpec = tween(slide)) { fullWidth -> -fullWidth }
        },
        popExitTransition = {
            slideOutHorizontally(animationSpec = tween(slide)) { fullWidth -> fullWidth }
        }
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginOk = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0) // limpia el back stack; no vuelves a Login con back
                    }
                },
                onGoToRegister = { navController.navigate(AppRoute.Register.route) }
            )
        }

        composable(AppRoute.Register.route) {
            RegisterScreen(
                onRegisterOk = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Home.route) {
            HomeRoute(
                onAddPlant = { navController.navigate(AppRoute.PlantForm.createRoute()) },
                onEditPlant = { id -> navController.navigate(AppRoute.PlantForm.createRoute(id)) },
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(
            route = AppRoute.PlantForm.route,
            arguments = listOf(
                navArgument(AppRoute.PlantForm.ARG_PLANT_ID) {
                    type = NavType.LongType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getLong(AppRoute.PlantForm.ARG_PLANT_ID)
            PlantFormScreen(
                plantId = plantId,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}