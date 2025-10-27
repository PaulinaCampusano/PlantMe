
package com.example.plantme_grupo8.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.menuAnchor
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null // Agregamos el parametro que usaremos en la funcion
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
            // Icono de editar (para ejecutar el callback)
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar")
                }
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

    // Planta en edición (null = no editando)
    var editing by remember { mutableStateOf<ModelPlant?>(null) }

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
                        onDelete = { vm.deletePlant(p.id) },  // borrar desde el VM
                        onEdit   = { editing = p } // abre diálogo de edición
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    //EJECUTAR EL DIALOGO PARA EDITAR
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

//INICIO DE INTERFAZ PARA EDITAR ITEM
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlantDialog(
    plant: ModelPlant,
    onDismiss: () -> Unit,
    onSave: (name: String, speciesKey: String, lastWateredAtMillis: Long) -> Unit
) {
    val speciesList = SpeciesDefault.list

    var name by remember(plant.id) { mutableStateOf(plant.name) }

    // Forzamos String no-null (si plant.speciesKey es String?)
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
                        value = SpeciesDefault.displayFor(selectedKey) ?: "", // En caso que devuelva un string
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de planta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ddExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = ddExpanded,
                        onDismissRequest = { ddExpanded = false }
                    ) {
                        speciesList.forEach { sp ->
                            DropdownMenuItem(
                                text = { Text(sp.display) },
                                onClick = {
                                    selectedKey = sp.key ?: ""   // por si sp.key es String?
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
            TextButton(
                onClick = {
                    // selectedKey es String no-null
                    onSave(name.trim(), selectedKey, lastMillis)
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
//FINAL DE INTERFAZ PARA EDITAR ITEM

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "Paulina Campusano")
}