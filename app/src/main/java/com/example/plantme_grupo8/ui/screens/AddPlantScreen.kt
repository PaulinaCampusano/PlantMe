@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.plantme_grupo8.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.R
import com.example.plantme_grupo8.ui.theme.utils.SpeciesDefault
import com.example.plantme_grupo8.viewModel.PlantsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun AddPlantScreen(
    vm: PlantsViewModel,       // <-- Coincide con AppNavHost
    onBack: () -> Unit,        // <-- Coincide con AppNavHost (sirve para cancelar y para salir al guardar)
    @DrawableRes photoRes: Int? = R.drawable.backagroudhs
) {
    val context = LocalContext.current

    // Estados del formulario
    var name by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }

    // Dropdown Especies
    var expanded by remember { mutableStateOf(false) }
    var selectedKey by rememberSaveable { mutableStateOf("cactus") } // Valor por defecto seguro
    val speciesList = SpeciesDefault.getAllKeys()

    // Fechas (Riego)
    val calendar = remember { Calendar.getInstance() }
    var lastWateredMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Estado de carga (Backend)
    var isLoading by remember { mutableStateOf(false) }

    // Formateador para mostrar la fecha bonita en pantalla
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Imagen de Fondo
        if (photoRes != null) {
            Image(
                painter = painterResource(id = photoRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fondo gris oscuro si no hay imagen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2D3B2D))
            )
        }

        // 2. Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(), // Evita que se solape con la barra de estado
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Nueva Planta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // -- CAMPO NOMBRE --
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Nombre", color = Color.White) },
                isError = nameError,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) {
                Text("El nombre no puede estar vacío", color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -- DROPDOWN ESPECIE --
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = SpeciesDefault.displayFor(selectedKey) ?: selectedKey,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Especie", color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
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

            Spacer(modifier = Modifier.height(16.dp))

            // -- SELECCIONAR FECHA DE RIEGO --
            Text("Último riego:", color = Color.White, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dateFormat.format(lastWateredMillis),
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        // Diálogo Fecha
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                // Diálogo Hora (después de fecha)
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        calendar.set(Calendar.MINUTE, minute)
                                        lastWateredMillis = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Cambiar fecha",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // -- BOTONES --
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Botón Cancelar
                OutlinedButton(
                    onClick = onBack, // Vuelve atrás
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading // Desactivar si está cargando
                ) {
                    Text("Cancelar", color = Color.White)
                }

                // Botón Guardar
                Button(
                    onClick = {
                        nameError = name.isBlank()
                        if (!nameError) {
                            isLoading = true

                            // 1. Convertir milisegundos a String ISO para el Backend
                            // El backend espera: "2025-12-01T10:00:00"
                            val instant = java.time.Instant.ofEpochMilli(lastWateredMillis)
                            val zoneId = java.time.ZoneId.systemDefault()
                            val isoDate = instant.atZone(zoneId).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                            // 2. Llamada al ViewModel
                            vm.createPlant(
                                nombre = name.trim(),
                                speciesKey = selectedKey,
                                ultimoRiego = isoDate,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "¡Planta Guardada!", Toast.LENGTH_SHORT).show()
                                    onBack() // Volver al Home
                                },
                                onError = { errorMsg ->
                                    isLoading = false
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Verde bonito
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}