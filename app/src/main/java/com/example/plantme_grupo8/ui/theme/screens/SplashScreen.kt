package com.example.plantme_grupo8.ui.theme.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.plantme_grupo8.R
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun SplashScreen(onDone: () -> Unit,@DrawableRes photoRes: Int? = R.drawable.backagroudhs) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(600), label = "splash-scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600), label = "splash-alpha"
    )

    // Dispara animación y luego navega
    LaunchedEffect(Unit) {
        visible = true
        delay(1000)          // “carga” breve (1s)
        onDone()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Tu fondo (opcional)
        if (photoRes != null) {
        Image(
            painter = painterResource(id = photoRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )}
        // Logo/título con fade + escala
        Text(
            "PlantMe",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha }
                .scale(scale)
        )
    }
}
