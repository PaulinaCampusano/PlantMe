@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.plantme_grupo8.ui.theme.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.ui.theme.utils.Species
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.OutlinedTextFieldDefaults


/* -----------------------------------------------------------
   Selector de especie (top-level)
   ----------------------------------------------------------- */
@Composable
private fun SpeciesSelector(
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
            label = { Text("Tipo de planta",color = Color.White) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ddExpanded) },
            modifier = Modifier
                .menuAnchor()             // si da error en tu versión, elimina esta línea
                .fillMaxWidth() ,
            colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.White,  // borde cuando está activo
            unfocusedBorderColor = Color.White   // borde cuando NO está activo
        )
        )

        DropdownMenu(
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

/* -----------------------------------------------------------
   Pantalla: Agregar planta
   ----------------------------------------------------------- */
@Composable
fun AddPlantScreen(
    homeVm: PlantsViewModel,
    onSaved: () -> Unit = {},
    onCancel: () -> Unit = {},
    @DrawableRes photoRes: Int? = R.drawable.add_header
) {
    // Estado UI
    var name by rememberSaveable { mutableStateOf("") }
    val speciesList = SpeciesDefault.list
    var selectedKey by rememberSaveable { mutableStateOf(speciesList.firstOrNull()?.key ?: "") }
    var lastWateredMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var nameError by remember { mutableStateOf(false) }

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

    // ======= LAYOUT PRINCIPAL =======
    Column(
        modifier = Modifier
            .fillMaxSize()

    ) {
        // ---------- HEADER ----------
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
            // Scrim general
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x33000000))
            )
            // Gradiente superior
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
            // Título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(29.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Agregar planta",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- PADDING ENTRE HEADER Y CONTENIDO ----------
        Spacer(Modifier.height(16.dp))

        // ---------- CONTENIDO (FORM) ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),



            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // INICIO TEXTBOX NOMBRE PLANTA
            OutlinedTextField(
                value = name, // <- NECESARIO para que el nombre se edite correctamente
                onValueChange = { name = it; nameError = false },
                label = { Text("Nombre de la planta", color = Color.White) },
                singleLine = true,
                isError = nameError,
                supportingText = { if (nameError) Text("Ingresa un nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.White,  // borde cuando está activo
                    unfocusedBorderColor = Color.White   // borde cuando NO está activo


                )
            )
            // FIN TEXTBOX NOMBRE PLANTA

            // Tipo de planta
            SpeciesSelector(
                speciesList = speciesList,
                selectedKey = selectedKey,
                onSelected = { selectedKey = it },
                modifier = Modifier.fillMaxWidth(),


            )

            // Último riego
            OutlinedTextField(
                value = lastText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Último riego (fecha y hora)", color = Color.White) },
                trailingIcon = {
                    Row {
                        TextButton(onClick = { pickDate() }) { Text("Fecha") }
                        TextButton(onClick = { pickTime() }) { Text("Hora") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color.White,  // borde cuando está activo
                    unfocusedBorderColor = Color.White   // borde cuando NO está activo
                )
            )

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    border = BorderStroke(1.dp, Color.White),               // color y grosor del borde
                    // shape = RoundedCornerShape(12.dp) // (opcional) esquinas redondeadas
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar",color = Color.White)
                }
                Button(
                    onClick = {
                        nameError = name.isBlank()
                        if (!nameError) {
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
        }
    }
}
