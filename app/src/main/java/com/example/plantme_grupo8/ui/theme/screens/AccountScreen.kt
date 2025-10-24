package com.example.plantme_grupo8.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.viewModel.HomeViewModel

/*
 * Pantalla de Cuenta
 * - Muestra avatar, nombre y la CANTIDAD de plantas (lee homeVm.plants).
 * - Incluye 3 opciones con Switch (estado local con rememberSaveable).
 */

@Composable
fun AccountScreen(
    username: String,
    homeVm: HomeViewModel
) {
    // Leemos la lista de plantas del mismo VM que usa Home
    val plants by homeVm.plants.collectAsState()
    val plantCount = plants.size

    // Estado local de los switches (por ahora no lo persistimos)
    var pushReminders by rememberSaveable { mutableStateOf(true) }
    var largeText by rememberSaveable { mutableStateOf(false) }
    var highContrast by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        AccountHeader(
            username = username,
            plantCount = plantCount
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Preferencias",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SettingSwitchRow(
            title = "Recordatorios de riego",
            subtitle = "Recibir notificaciones cuando toque regar",
            checked = pushReminders,
            onCheckedChange = { pushReminders = it }
        )
        SettingSwitchRow(
            title = "Texto grande",
            subtitle = "Aumenta el tamaño del texto",
            checked = largeText,
            onCheckedChange = { largeText = it }
        )
        SettingSwitchRow(
            title = "Alto contraste",
            subtitle = "Mejora la legibilidad",
            checked = highContrast,
            onCheckedChange = { highContrast = it }
        )
    }
}

/* ------------------ UI helpers ------------------ */

@Composable
fun AccountHeader(
    username: String,
    plantCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF34D399), Color(0xFF065F46))
                )
            )
            .padding(top = 40.dp, start = 18.dp, end = 18.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    username,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$plantCount plantas registradas",
                    color = Color(0xFFEAFBF2),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle != null) Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        modifier = Modifier.fillMaxWidth()
    )
    Divider()
}
