package com.example.plantme_grupo8.ui.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import java.util.*
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    vm: PlantsViewModel,
    @DrawableRes photoRes: Int? = R.drawable.backagroudhs
) {
    val plants by vm.plants.collectAsState()
    // val dueIds by vm.dueIds.collectAsState() // Ya no necesitamos esto para el color, usaremos la fecha real
    val context = LocalContext.current

    // Permisos de Notificación
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        vm.loadPlantsFromServer() // Cargar lista al abrir
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, $username", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("Mis Plantas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo
            if (photoRes != null) {
                Image(painter = painterResource(id = photoRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(Color(0xFF2D3B2D)))
            }

            // Lista
            if (plants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("No tienes plantas aún.\n¡Agrega una nueva!", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(plants) { plant ->
                        PlantItem(
                            plant = plant,
                            onWater = { vm.waterPlant(plant) }, // Llama al backend
                            onEdit = { Toast.makeText(context, "Editar próximamente", Toast.LENGTH_SHORT).show() },
                            onDelete = { vm.deletePlant(plant) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun PlantItem(
    plant: ModelPlant,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // --- LÓGICA DE FECHAS NUEVA (Cuenta atrás) ---
    val now = System.currentTimeMillis()
    val diffMillis = plant.nextWateringAtMillis - now

    // Convertimos a días (redondeando hacia arriba)
    val daysLeft = ceil(diffMillis / (1000.0 * 60 * 60 * 24)).toInt()

    val statusText = when {
        daysLeft < 0 -> "¡Atrasado ${Math.abs(daysLeft)} días!"
        daysLeft == 0 -> "¡Hoy toca riego!"
        daysLeft == 1 -> "Riego mañana"
        else -> "Riego en $daysLeft días"
    }

    // Rojo si es hoy o antes, Verde si falta
    val isDue = daysLeft <= 0
    val statusColor = if (isDue) Color.Red else Color(0xFF4CAF50)

    val speciesName = SpeciesDefault.displayFor(plant.speciesKey ?: "") ?: plant.speciesKey ?: "Planta"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gota de agua
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDue) Color(0xFFFFEBEE) else Color(0xFFE3F2FD))
            ) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = if (isDue) Color.Red else Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(text = plant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = speciesName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                // AQUÍ MUESTRA "Riego en X días"
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botones
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null, tint = Color.Gray) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Color.Gray) }

                // BOTÓN REGAR
                FilledIconButton(
                    onClick = onWater,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Outlined.WaterDrop, null, tint = Color.White)
                }
            }
        }
    }
}