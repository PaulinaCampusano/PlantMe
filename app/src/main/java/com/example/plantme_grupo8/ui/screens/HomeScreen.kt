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
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    vm: PlantsViewModel,
    @DrawableRes photoRes: Int? = R.drawable.backagroudhs
) {
    val plants by vm.plants.collectAsState()
    val dueIds by vm.dueIds.collectAsState()
    val context = LocalContext.current

    // Permisos de Notificación (Android 13+)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                // Opcional: Avisar al usuario que sin permiso no habrá alertas
            }
        }
    )

    // Al iniciar, verifica permisos y recarga plantas del servidor
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // Escanear para notificaciones locales
        vm.scanAndNotify()
        // Recargar del servidor (por si acaso)
        vm.loadPlantsFromServer()
    }

    // Lógica para el Diálogo de Editar (Suspendido temporalmente por falta de Backend)
    var showEditDialog by remember { mutableStateOf(false) }
    var plantToEdit by remember { mutableStateOf<ModelPlant?>(null) }

    if (showEditDialog && plantToEdit != null) {
        EditPlantDialog(
            plant = plantToEdit!!,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newKey, newLastWater ->
                // ⚠️ TODO: Implementar endpoint PUT en Backend y función updatePlant en ViewModel
                // Por ahora, solo mostramos mensaje para que no crashee
                Toast.makeText(context, "Edición en desarrollo (Backend)", Toast.LENGTH_SHORT).show()

                /* CÓDIGO COMENTADO HASTA QUE EL BACKEND LO SOPORTE:
                vm.updatePlant(
                    plant = plantToEdit!!.copy(
                        name = newName,
                        speciesKey = newKey,
                        lastWateringAtMillis = newLastWater
                    )
                )
                */
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hola, $username",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Mis Plantas",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navegar a notificaciones si existiera pantalla */ }) {
                        Icon(Icons.Outlined.Notifications, null, tint = Color.White)
                    }
                    IconButton(onClick = { /* Navegar a perfil si se desea */ }) {
                        Icon(Icons.Outlined.Face, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Para que el fondo cubra todo
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de pantalla
            if (photoRes != null) {
                Image(
                    painter = painterResource(id = photoRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(Color(0xFF2D3B2D)))
            }

            // Lista de Plantas
            if (plants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes plantas aún.\n¡Agrega una nueva!",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(plants) { plant ->
                        val isDue = plant.id in dueIds || plant.isDue // Combina estado local y del modelo
                        PlantItem(
                            plant = plant,
                            isDue = isDue,
                            onWater = {
                                // CORRECCIÓN: Usamos waterPlant (el nombre nuevo)
                                vm.waterPlant(plant)
                            },
                            onEdit = {
                                plantToEdit = plant
                                showEditDialog = true
                            },
                            onDelete = {
                                vm.deletePlant(plant)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para el BottomBar
                }
            }
        }
    }
}

@Composable
fun PlantItem(
    plant: ModelPlant,
    isDue: Boolean,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Calculamos cuánto tiempo ha pasado
    val now = System.currentTimeMillis()
    val diff = now - plant.lastWateringAtMillis
    val delayText = formatDelay(diff)

    // CORRECCIÓN: Usamos SpeciesDefault para obtener el nombre bonito
    val speciesName = SpeciesDefault.displayFor(plant.speciesKey ?: "") ?: plant.speciesKey ?: "Planta"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono / Gota
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

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$speciesName • $delayText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (isDue) {
                    Text(
                        text = "¡Necesita agua!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botones de acción
            Row {
                // Editar
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
                // Borrar
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = Color.Gray)
                }
                // Regar (Botón principal)
                FilledIconButton(
                    onClick = onWater,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Outlined.WaterDrop, contentDescription = "Regar", tint = Color.White)
                }
            }
        }
    }
}

// --- DIÁLOGO DE EDICIÓN (UI) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantDialog(
    plant: ModelPlant,
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var name by remember { mutableStateOf(plant.name) }
    var selectedKey by remember { mutableStateOf(plant.speciesKey ?: "cactus") }
    var lastMillis by remember { mutableStateOf(plant.lastWateringAtMillis) }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }
    val speciesList = SpeciesDefault.getAllKeys()

    // Date/Time Pickers logic
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val lastText = dateFormat.format(Date(lastMillis))

    fun pickDate() {
        calendar.timeInMillis = lastMillis
        DatePickerDialog(context, { _, y, m, d ->
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)
            lastMillis = calendar.timeInMillis
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun pickTime() {
        calendar.timeInMillis = lastMillis
        TimePickerDialog(context, { _, h, min ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, min)
            lastMillis = calendar.timeInMillis
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Planta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Especie
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = SpeciesDefault.displayFor(selectedKey) ?: selectedKey,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Especie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        speciesList.forEach { key ->
                            DropdownMenuItem(
                                text = { Text(SpeciesDefault.displayFor(key) ?: key) },
                                onClick = {
                                    selectedKey = key
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Fecha Último Riego
                OutlinedTextField(
                    value = lastText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Último riego") },
                    trailingIcon = {
                        Row {
                            TextButton(onClick = { pickDate() }) { Text("Fecha") }
                            TextButton(onClick = { pickTime() }) { Text("Hora") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), selectedKey, lastMillis) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/* Helper para "hace X tiempo" */
private fun formatDelay(ms: Long): String {
    val totalSec = (ms / 1000).toInt()
    val min = totalSec / 60
    val hr = min / 60
    val day = hr / 24
    return when {
        day > 0 -> "hace ${day}d ${hr % 24}h"
        hr > 0 -> "hace ${hr}h ${min % 60}m"
        min > 0 -> "hace ${min}m"
        else -> "hace segundos"
    }
}