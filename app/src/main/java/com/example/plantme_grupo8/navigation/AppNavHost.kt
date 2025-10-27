package com.example.plantme_grupo8.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.plantme_grupo8.ui.theme.screens.*
import com.example.plantme_grupo8.viewModel.AuthViewModel
import com.example.plantme_grupo8.viewModel.PlantsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authVm: AuthViewModel,
    plantsVm: PlantsViewModel // ← sin coma final
) {
    val logged by authVm.isLoggedIn.collectAsState()
    val session by authVm.session.collectAsState()
    val username = session?.name ?: "Invitado"

    if (logged) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(username = username, vm = plantsVm) // pasa PlantsViewModel
            }
            composable(AppRoute.Add.route) {
                AddPlantScreen(
                    homeVm = plantsVm,
                    onSaved = {
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
                AccountScreen(
                    username = username,
                    homeVm = plantsVm,
                    onLogout = { authVm.logout() },
                    onDeleteAccount = { authVm.deleteLocalAccount() }
                )
            }
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Login.route
        ) {
            composable(AppRoute.Login.route) {
                LoginScreen(
                    vm = authVm,
                    onGoRegister = { navController.navigate(AppRoute.Register.route) },
                    onLoggedIn = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Register.route) {
                RegisterScreen(
                    vm = authVm,
                    onRegistered = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
