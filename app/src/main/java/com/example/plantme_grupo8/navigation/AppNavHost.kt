package com.example.plantme_grupo8.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.plantme_grupo8.viewModel.HomeViewModel
import com.example.plantme_grupo8.ui.theme.screens.AccountScreen
import com.example.plantme_grupo8.ui.theme.screens.AddPlantScreen
import com.example.plantme_grupo8.ui.theme.screens.HomeScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    homeVm: HomeViewModel,
    username: String
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(username = username, vm = homeVm)
        }
        composable(AppRoute.Add.route) {
            AddPlantScreen(
                homeVm = homeVm,
                onSaved = {
                    // vuelve a Home al guardar
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Account.route) {
            AccountScreen(username = username, homeVm = homeVm)
        }
    }
}