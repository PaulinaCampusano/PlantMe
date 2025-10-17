package com.example.plantme_grupo8.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.viewModel.HomeViewModel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.example.plantme_grupo8.ui.theme.utils.formatRemaining
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel



@Composable
fun HomeHeader(username: String) {
    // Caja de fondo alto 140dp; pintamos el degradado y metemos el contenido encima.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF34D399), Color(0xFF065F46))
                )
            )
            .padding(top = 32.dp, start = 18.dp, end = 18.dp, bottom = 12.dp)
    ) {
        // Fila: avatar a la izquierda, texto a la derecha
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun PlantCard(
    plant: ModelPlant,
    onDelete: (() -> Unit)? = null
) {
    // Si te marca en rojo mutableLongStateOf, cámbialo por mutableStateOf(0L)
    var remainingMs by remember(plant.id) { mutableStateOf(0L) }

    LaunchedEffect(plant.nextWateringAtMillis) {
        while (true) {
            remainingMs = plant.nextWateringAtMillis - System.currentTimeMillis()
            delay(1_000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = plant.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Regar en ${formatRemaining(remainingMs)}")
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
@Composable
fun HomeScreen(
    username: String,
    vm: HomeViewModel = viewModel()  // mismo VM que usará la screen de “Nueva planta”
) {
    val plants by vm.plants.collectAsState()  // observamos la lista

    Column(Modifier.fillMaxSize()) {
        HomeHeader(username)                 // 1) tu header

        if (plants.isEmpty()) {              // 2) estado vacío
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no has agregado plantas.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {                             // 3) lista de cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(plants, key = { it.id }) { p: ModelPlant ->
                    PlantCard(
                        plant = p,
                        onDelete = { vm.deletePlant(p.id) }  // borrar desde el VM
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "Paulina Campusano")
}



