package com.example.plantme_grupo8.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.plantme_grupo8.viewModel.HomeViewModel

@Composable
fun AppRoot(
    homeVm: HomeViewModel,
    username: String
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomBar(navController) }   // BottomBar global
    ) { inner ->
        Box(Modifier.padding(inner)) {
            AppNavHost(
                navController = navController,
                homeVm = homeVm,
                username = username
            )
        }
    }
}