package com.example.plantme_grupo8.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.viewModel.PlantsViewModel

@Composable
fun AccountScreen(
    username: String,
    homeVm: PlantsViewModel,
    onLogout: () -> Unit,              // ⬅️ nuevo
    onDeleteAccount: () -> Unit        // ⬅️ nuevo
) {
    val plants by homeVm.plants.collectAsState()
    val plantCount = plants.size

    var pushReminders by remember { mutableStateOf(true) }
    var largeText by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        AccountHeader(username = username, plantCount = plantCount)

        Spacer(Modifier.height(12.dp))



        // --- Acciones de cuenta ---
        Button(
            onClick = onLogout,     // ⬅️ usa callback
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) { Text("Cerrar sesión") }


    }
}

/* ---- helpers visuales (sin cambios) ---- */

@Composable
fun AccountHeader(username: String, plantCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 18.dp, end = 18.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF124A4E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(username, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("$plantCount plantas registradas", color = Color(0xFFEAFBF2))
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
