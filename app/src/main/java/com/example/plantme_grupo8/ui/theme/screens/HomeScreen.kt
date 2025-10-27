package com.example.plantme_grupo8.ui.theme.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.model.ModelPlant
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.ui.theme.utils.formatRemaining
import com.example.plantme_grupo8.viewModel.HomeViewModel
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* -----------------------------------------------------------
   HEADER: foto + gradiente + título y campana
   ----------------------------------------------------------- */
@Composable
fun HomeHeader(
    username: String,
    @DrawableRes photoRes: Int? = R.drawable.home_header,
    onBellClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (photoRes != null) {
            Image(
                painter = painterResource(id = photoRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0x33000000))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xCC000000),
                            0.35f to Color(0x99000000),
                            0.70f to Color(0x33000000),
                            1.00f to Color(0x33000000)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(29.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = username,
                color = Color.White,
                fontSize = 27.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color.White
                )
            }
        }
    }
}

/* -----------------------------------------------------------
   CARD de planta: ahora soporta estado "due" e ícono de agua
   ----------------------------------------------------------- */
@Composable
fun PlantCard(
    plant: ModelPlant,
    isDue: Boolean = false,            // <- aparece tras notificación
    onWater: (() -> Unit)? = null,     // <- reinicia contador
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    var remainingMs by remember(plant.id) { mutableStateOf(0L) }

    LaunchedEffect(plant.nextWateringAtMillis) {
        while (true) {
            remainingMs = plant.nextWateringAtMillis - System.currentTimeMillis()
            delay(1_000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(2.dp))

                val label = if (isDue) {
                    "Retrasado ${formatDelay((-remainingMs).coerceAtLeast(0L))}"
                } else {
                    "Regar en ${formatRemaining(remainingMs)}"
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B5563)
                )
            }

            // solo visible cuando hubo notificación
            if (isDue && onWater != null) {
                IconButton(onClick = onWater) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Regado listo"
                    )
                }
            }

            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

/* -----------------------------------------------------------
   HOME SCREEN
   ----------------------------------------------------------- */
@Composable
fun HomeScreen(
    username: String,
    vm: PlantsViewModel = viewModel()
) {
    val plants by vm.plants.collectAsState()
    val dueIds by vm.dueIds.collectAsState()
    var editing by remember { mutableStateOf<ModelPlant?>(null) }

    // ---- Permiso de notificaciones (Android 13+) ----
    val context = LocalContext.current
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted -> no-op */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // ---- Escaneo periódico para notificar y marcar "due" ----
    LaunchedEffect(Unit) {
        while (true) {
            vm.scanAndNotify()
            delay(5_000)
        }
    }

    // Fondo gris claro como en el mock
    val screenBg = Color(0xFFF3F4F6)

    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        HomeHeader(username = username)

        if (plants.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no has agregado plantas.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(plants, key = { it.id }) { p ->
                    val isDue = dueIds.contains(p.id)
                    PlantCard(
                        plant = p,
                        isDue = isDue,
                        onWater = { vm.waterNow(p.id) },
                        onDelete = { vm.deletePlant(p.id) },
                        onEdit = { editing = p }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }

    // Diálogo de edición
    editing?.let { plant ->
        EditPlantDialog(
            plant = plant,
            onDismiss = { editing = null },
            onSave = { name, speciesKey, lastMillis ->
                vm.updatePlant(
                    id = plant.id,
                    name = name,
                    speciesKey = speciesKey,
                    lastWateredAtMillis = lastMillis
                )
                editing = null
            }
        )
    }
}

/* -----------------------------------------------------------
   DIÁLOGO EDITAR (igual, con import de menuAnchor habilitado)
   ----------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlantDialog(
    plant: ModelPlant,
    onDismiss: () -> Unit,
    onSave: (name: String, speciesKey: String, lastWateredAtMillis: Long) -> Unit
) {
    val speciesList = SpeciesDefault.list
    var name by remember(plant.id) { mutableStateOf(plant.name) }
    var selectedKey by remember(plant.id) { mutableStateOf(plant.speciesKey ?: "") }

    val DAY_MS = 24L * 60 * 60 * 1000
    var lastMillis by remember(plant.id) {
        mutableStateOf(plant.nextWateringAtMillis - plant.intervalDays * DAY_MS)
    }

    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val lastText = remember(lastMillis) { fmt.format(Date(lastMillis)) }

    val ctx = LocalContext.current
    fun pickDate() {
        val c = Calendar.getInstance().apply { timeInMillis = lastMillis }
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                c.set(Calendar.YEAR, y)
                c.set(Calendar.MONTH, m)
                c.set(Calendar.DAY_OF_MONTH, d)
                lastMillis = c.timeInMillis
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    fun pickTime() {
        val c = Calendar.getInstance().apply { timeInMillis = lastMillis }
        TimePickerDialog(
            ctx,
            { _, h, min ->
                c.set(Calendar.HOUR_OF_DAY, h)
                c.set(Calendar.MINUTE, min)
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)
                lastMillis = c.timeInMillis
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }

    var ddExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar planta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = ddExpanded,
                    onExpandedChange = { ddExpanded = !ddExpanded }
                ) {
                    OutlinedTextField(
                        value = SpeciesDefault.displayFor(selectedKey) ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de planta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ddExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    androidx.compose.material3.DropdownMenu(
                        expanded = ddExpanded,
                        onDismissRequest = { ddExpanded = false }
                    ) {
                        speciesList.forEach { sp ->
                            DropdownMenuItem(
                                text = { Text(sp.display) },
                                onClick = {
                                    selectedKey = sp.key ?: ""
                                    ddExpanded = false
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
            TextButton(onClick = { onSave(name.trim(), selectedKey, lastMillis) }) {
                Text("Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/* -----------------------------------------------------------
   Helper para "Retrasado ..."
   ----------------------------------------------------------- */
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "Paulina Campusano")
}
