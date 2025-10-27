package com.example.plantme_grupo8.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.plantme_grupo8.viewModel.AuthViewModel
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.plantme_grupo8.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize


@Composable
fun AppRoot( @DrawableRes photoRes: Int? = R.drawable.backagroudhs) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val plantsVm: PlantsViewModel = viewModel()

    val logged by authVm.isLoggedIn.collectAsState()

    Scaffold(
        bottomBar = { if (logged) AppBottomBar(navController) }
    ) { inner ->
        Box(Modifier.padding(inner)) {

            if (photoRes != null) {
                Image(
                    painter = painterResource(id = photoRes), // tu archivo en res/drawable/
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppNavHost(
                navController = navController,
                authVm = authVm,
                plantsVm = plantsVm
            )
        }
    }
}
