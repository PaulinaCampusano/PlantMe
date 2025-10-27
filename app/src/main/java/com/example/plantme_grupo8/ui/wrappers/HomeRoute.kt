package com.example.plantme_grupo8.ui.wrappers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.plantme_grupo8.ui.theme.screens.HomeScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onAddPlant: () -> Unit,
    onEditPlant: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    username: String = "Usuario"
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PlantMe") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlant) {
                Icon(Icons.Default.Add, contentDescription = "Agregar planta")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            HomeScreen(username = username)
        }
    }
}
