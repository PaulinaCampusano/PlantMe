package com.example.plantme_grupo8

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.plantme_grupo8.viewModel.HomeViewModel
import com.example.plantme_grupo8.navigation.AppRoot

@Composable
fun PlantMeApp(
    homeVm: HomeViewModel,
    username: String
) {
    // Si quieres, puedes envolver con el theme aquí en vez de MainActivity
    // PlantMe_Grupo8Theme {
    //     AppRoot(homeVm = homeVm, username = username)
    // }

    AppRoot(homeVm = homeVm, username = username)
}
