//PARA EL DROPMENU SE DEBE DECLARAR QUE SE VA A USAR MATERIAL 3
@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.plantme_grupo8.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.plantme_grupo8.ui.theme.utils.Species
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.viewModel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddPlantScreen(
    homeVm: HomeViewModel,         // MISMO VM que usa Home
    onSaved: () -> Unit = {},      // navega atrás luego (cuando tengas Nav)
    onCancel: () -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) } // (no se usa; puedes borrarlo si quieres)

    // Tipo seleccionado (por defecto el primero)
    val speciesList = SpeciesDefault.list
    var selectedKey by rememberSaveable { mutableStateOf(speciesList.firstOrNull()?.key ?: "") }

    // Último riego (por defecto ahora)
    var lastWateredMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Errores simples
    var nameError by remember { mutableStateOf(false) }

    // Pickers
    val ctx = LocalContext.current
    fun pickDate() {
        val c = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                c.set(Calendar.YEAR, y)
                c.set(Calendar.MONTH, m)
                c.set(Calendar.DAY_OF_MONTH, d)
                lastWateredMillis = c.timeInMillis
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    fun pickTime() {
        val c = Calendar.getInstance().apply { timeInMillis = lastWateredMillis }
        TimePickerDialog(
            ctx,
            { _, h, min ->
                c.set(Calendar.HOUR_OF_DAY, h)
                c.set(Calendar.MINUTE, min)
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)
                lastWateredMillis = c.timeInMillis
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }

    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val lastText = remember(lastWateredMillis) { fmt.format(Date(lastWateredMillis)) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // INICIO TITULO
        Text("Agregar planta", style = MaterialTheme.typography.headlineSmall)
        // FIN TITULO

        // INICIO TEXTBOX NOMBRE PLANTA
        OutlinedTextField(
            value = name, // <- NECESARIO para que el nombre se edite correctamente
            onValueChange = { name = it; nameError = false },
            label = { Text("Nombre de la planta") },
            singleLine = true,
            isError = nameError,
            supportingText = { if (nameError) Text("Ingresa un nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        // FIN TEXTBOX NOMBRE PLANTA

        // INICIO TIPO DE PLANTA
        @Composable
        fun SpeciesSelector(
            speciesList: List<Species>,
            selectedKey: String,
            onSelected: (String) -> Unit,
            modifier: Modifier = Modifier
        ) {
            var ddExpanded by rememberSaveable { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = ddExpanded,
                onExpandedChange = { ddExpanded = !ddExpanded },
                modifier = modifier
            ) {
                OutlinedTextField(
                    value = SpeciesDefault.displayFor(selectedKey),
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
                                onSelected(sp.key)
                                ddExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // llamada al selector (NECESARIA para actualizar selectedKey)
        SpeciesSelector(
            speciesList = speciesList,
            selectedKey = selectedKey,
            onSelected = { selectedKey = it },
            modifier = Modifier.fillMaxWidth()
        )
        // FINAL TIPO DE PLANTA

        // INICIO ULTIMO RIEGO
        OutlinedTextField(
            value = lastText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Último riego (fecha y hora)") },
            trailingIcon = {
                Row {
                    TextButton(onClick = { pickDate() }) { Text("Fecha") }
                    TextButton(onClick = { pickTime() }) { Text("Hora") }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        // FINAL ULTIMO RIEGO

        // INICIO BOTONES CANCELAR Y GUARDAR
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    nameError = name.isBlank()
                    if (!nameError) {
                        // AUTOMÁTICO: calcula intervalo por tipo y guarda
                        homeVm.addPlantAuto(
                            name = name.trim(),
                            speciesKey = selectedKey,
                            lastWateredAtMillis = lastWateredMillis
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
        // FINAL BOTONES CANCELAR Y GUARDAR
    }
}
