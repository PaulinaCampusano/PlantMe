package com.example.plantme_grupo8.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(username: String) {
    // Fondo con un degradado verde (arriba claro, abajo oscuro)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF34D399), // verde claro
                        Color(0xFF065F46)  // verde oscuro
                    )
                )
            )
            .padding(top = 32.dp, start = 18.dp, end = 18.dp, bottom = 12.dp)
    ) {
        // Fila: ícono a la izquierda, textos a la derecha
        Row(verticalAlignment = Alignment.CenterVertically) {

            // Círculo con ícono de persona
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000)), // gris transparente
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.width(16.dp)) // espacio entre icono y texto

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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(username = "Paulina Campusano")
}



