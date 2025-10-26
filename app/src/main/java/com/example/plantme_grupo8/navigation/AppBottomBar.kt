package com.example.plantme_grupo8.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun AppBottomBar(navController: NavController) {
    val items = listOf(
        Triple(AppRoute.Home, Icons.Default.Home,  "Inicio"),
        Triple(AppRoute.Add,  Icons.Default.Add,   "Agregar"),
        Triple(AppRoute.Account, Icons.Default.Person, "Cuenta")
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    NavigationBar {
        items.forEach { (route, icon, label) ->
            val selected = current?.hierarchy?.any { it.route == route.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}