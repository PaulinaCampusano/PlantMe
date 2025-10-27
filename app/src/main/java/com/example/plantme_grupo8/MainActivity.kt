package com.example.plantme_grupo8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.plantme_grupo8.ui.theme.PlantMe_Grupo8Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlantMe_Grupo8Theme {
                PlantMeApp()   // <- sin argumentos
            }
        }
    }
}
