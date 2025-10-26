package com.example.plantme_grupo8

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.plantme_grupo8.navigation.AppNavHost

@Composable
fun PlantMeApp() {
    Surface(color = MaterialTheme.colorScheme.background) {
        AppNavHost()
    }
}
