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
    val logged by authVm.isUserLoggedFlow.collectAsState()

    val username by authVm.usernameFlow.collectAsState()

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
        // --- FLUJO CUANDO EL USUARIO NO ESTÁ LOGUEADO ---
        NavHost(
            navController = navController,
            startDestination = AppRoute.Login.route
        ) {
            composable(AppRoute.Login.route) {
                LoginScreen(
                    vm = authVm,
                    onGoRegister = { navController.navigate(AppRoute.Register.route) },

                    // 🔥 CORRECCIÓN AQUÍ:
                    // No navegamos manualmente. Solo dejamos el bloque vacío o loggeamos algo.
                    // Al cambiar 'logged' a true, el 'if' de arriba se activará y mostrará el Home solo.
                    onLoggedIn = {
                        // NO HACER NADA AQUÍ (Opcional: un println de debug)
                        // navController.navigate(...) <--- ESTO CAUSABA EL CRASH
                    }
                )
            }
            composable(AppRoute.Register.route) {
                RegisterScreen(
                    vm = authVm,
                    onRegistered = {
                        // Aquí sí podríamos navegar, o dejar que el usuario vaya al login manual
                        // Si tu AuthViewModel hace login automático al registrar, deja esto vacío.
                        // Si no, navegar al login está bien.
                        navController.popBackStack() // Volver al Login para que ingrese
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
