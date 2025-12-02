package com.example.plantme_grupo8.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.viewModel.AuthViewModel
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import com.example.plantme_grupo8.viewModel.UiBusViewModel

@Composable
fun AppRoot(@DrawableRes photoRes: Int? = R.drawable.backagroudhs) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val plantsVm: PlantsViewModel = viewModel()
    val uiBus: UiBusViewModel = viewModel()

    val logged by authVm.isUserLoggedFlow.collectAsState()
    val loading by uiBus.isLoading.collectAsState()

    Scaffold(
        bottomBar = { if (logged) AppBottomBar(navController) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {

            if (photoRes != null) {
                Image(
                    painter = painterResource(id = photoRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // uiBus ya no es parámetro de AppNavHost
            AppNavHost(
                navController = navController,
                authVm = authVm,
                plantsVm = plantsVm
            )

            // Overlay global del loader (sí usa uiBus aquí)
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
