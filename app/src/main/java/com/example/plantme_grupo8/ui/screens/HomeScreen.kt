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

import androidx.compose.material3.*

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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.text.font.FontWeight

import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault.getAllKeys
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault.getAllSpecies
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault.displayFor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    vm: PlantsViewModel,
    @DrawableRes photoRes: Int? = R.drawable.backagroudhs
) {
    val plants by vm.plants.collectAsState()
    val context = LocalContext.current

    // ESTADO PARA EL DIÁLOGO DE EDICIÓN
    var plantToEdit by remember { mutableStateOf<ModelPlant?>(null) }
    var plantToDelete by remember { mutableStateOf<ModelPlant?>(null) }

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
        vm.loadPlantsFromServer()
        vm.scanAndNotify()
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
            if (photoRes != null) {
                Image(painter = painterResource(id = photoRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(Color(0xFF2D3B2D)))
            }

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

                    items(
                        items = plants,
                        key = { it.id }
                    ) { plant ->
                        PlantItemWithTimer(
                            plant = plant,
                            onWater = { vm.waterPlant(plant) },
                            onEdit = { plantToEdit = plant },
                            onDelete = { plantToDelete = plant }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // DIÁLOGO DE EDICIÓN
    // ... dentro de HomeScreen
    plantToEdit?.let { plant ->
        EditPlantDialog(
            plant = plant,
            // AQUI faltaba el vm = vm
            onSave = { name, speciesKey, lastWateredMillis ->
                vm.updatePlant(plant.id, name, speciesKey, lastWateredMillis)
                // Cierra el diálogo después de guardar
                plantToEdit = null
            },
            onDismiss = {
                // Cierra el diálogo si se cancela
                plantToEdit = null
            }
        )
    }
// ...

    // DIÁLOGO DE ELIMINAR
    plantToDelete?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Eliminar Planta") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${plant.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deletePlant(plant)
                    plantToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PlantItemWithTimer(
    plant: ModelPlant,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = System.currentTimeMillis()
        }
    }

    val diff = plant.nextWateringAtMillis - now
    val isDue = diff <= 0
    val statusText = formatDuration(diff)
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

            Column(modifier = Modifier.weight(1f)) {
                Text(text = plant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = speciesName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    text = if (isDue) "¡Necesita Riego!" else statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Editar", tint = Color.Gray) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Eliminar", tint = Color.Gray) }
                FilledIconButton(
                    onClick = onWater,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Outlined.WaterDrop, "Regar", tint = Color.White)
                }
            }
        }
    }
}

// ====================================================================================
// COMPONENTE CORREGIDO: DIÁLOGO DE EDICIÓN DE PLANTA
// ====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantDialog(
    plant: ModelPlant,
    onSave: (name: String, speciesKey: String, lastWateredMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // 1. OBTENEMOS LA LISTA DE CLAVES (List<String>)
    val speciesKeys = SpeciesDefault.getAllKeys()

    var name by rememberSaveable { mutableStateOf(plant.name) }
    var selectedKey by rememberSaveable { mutableStateOf(plant.speciesKey) }
    var lastWateredMillis by rememberSaveable { mutableStateOf(plant.lastWateringAtMillis) }
    var expanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val pickDate: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val currentHour = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }.get(Calendar.HOUR_OF_DAY)
                val currentMinute = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }.get(Calendar.MINUTE)
                calendar.set(Calendar.HOUR_OF_DAY, currentHour)
                calendar.set(Calendar.MINUTE, currentMinute)
                lastWateredMillis = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val pickTime: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }
        TimePickerDialog(
            context,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                lastWateredMillis = calendar.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    val lastText by remember(lastWateredMillis) {
        mutableStateOf(
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(lastWateredMillis))
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Planta: ${plant.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la planta") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("El nombre no puede estar vacío") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth() // Quitamos .menuAnchor() de aquí si da problemas de versión
                ) {
                    val displayedSpeciesName = SpeciesDefault.displayFor(selectedKey ?: "") ?: selectedKey ?: ""

                    OutlinedTextField(
                        value = displayedSpeciesName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        label = { Text("Especie") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // FIX: Iteramos sobre la LISTA de keys
                        speciesKeys.forEach { key ->
                            val displayName = SpeciesDefault.displayFor(key) ?: key
                            DropdownMenuItem(
                                text = { Text(displayName) },
                                onClick = {
                                    selectedKey = key
                                    expanded = false
                                }
                            )
                        }
                    }
                }

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
            TextButton(
                onClick = {
                    nameError = name.isBlank()
                    if (!nameError) {
                        onSave(name.trim(), selectedKey.orEmpty(), lastWateredMillis)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

fun formatDuration(diffMillis: Long): String {
    val absDiff = abs(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(absDiff)
    val hours = TimeUnit.MILLISECONDS.toHours(absDiff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(absDiff) % 60

    return if (diffMillis > 0) {
        if (days > 0) "Riego en ${days}d ${hours}h ${minutes}m"
        else if (hours > 0) "Riego en ${hours}h ${minutes}m ${seconds}s"
        else "Riego en ${minutes}m ${seconds}s"
    } else {
        if (days > 0) "¡Atrasado por ${days}d ${hours}h!"
        else "¡Atrasado por ${hours}h ${minutes}m ${seconds}s!"
    }
}