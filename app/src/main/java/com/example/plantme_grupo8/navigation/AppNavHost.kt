package com.example.plantme_grupo8.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.plantme_grupo8.ui.screens.AccountScreen
import com.example.plantme_grupo8.ui.screens.AddPlantScreen
import com.example.plantme_grupo8.ui.screens.HomeScreen
import com.example.plantme_grupo8.ui.screens.LoginScreen
import com.example.plantme_grupo8.ui.screens.RegisterScreen
import com.example.plantme_grupo8.viewModel.AuthViewModel
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.plantme_grupo8.ui.screens.AccountScreen
import com.example.plantme_grupo8.ui.screens.AddPlantScreen
import com.example.plantme_grupo8.ui.screens.HomeScreen
import com.example.plantme_grupo8.ui.screens.LoginScreen
import com.example.plantme_grupo8.ui.screens.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    authVm: AuthViewModel,
    plantsVm: PlantsViewModel
) {
    // 1. CORRECCIÓN: Usamos la variable nueva 'isUserLogged'
    val logged by authVm.isUserLogged.collectAsState()

    // 2. CORRECCIÓN: Quitamos 'session'. Por ahora usaremos un nombre fijo.
    // (Más adelante podemos guardar el nombre en DataStore si quieres)
    val username = "Mi Jardín"

    if (logged) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route
        ) {
            composable(AppRoute.Home.route) {
                // Aquí pasamos el username fijo
                HomeScreen(username = username, vm = plantsVm)
            }

            // Ruta para agregar planta
            composable(AppRoute.Add.route) {
                AddPlantScreen(
                    vm = plantsVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppRoute.Account.route) {
                AccountScreen(
                    username = username,
                    homeVm = plantsVm, // Nota: AccountScreen pedía homeVm, le pasamos plantsVm si es compatible
                    onLogout = { authVm.logout() },
                    // Eliminamos deleteLocalAccount porque ahora es todo remoto
                    onDeleteAccount = { authVm.logout() }
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
                        // Al loguearse, vamos al Home y borramos el historial de atrás
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
                        // Al registrarse, vamos al Home
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
