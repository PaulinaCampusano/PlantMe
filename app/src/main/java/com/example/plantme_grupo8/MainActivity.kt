package com.example.plantme_grupo8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.plantme_grupo8.ui.theme.PlantMe_Grupo8Theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.plantme_grupo8.ui.theme.screens.HomeHeader


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantMe_Grupo8Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    HomeHeader(
                        username = "Paulina Campusano"
                        // Si quisieras usar el padding del Scaffold:
                        // modifier = Modifier.padding(innerPadding)
                    )
                    }
                }
            }
        }

        @Composable
        fun Greeting(name: String, modifier: Modifier = Modifier) {
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }

        @Preview(showBackground = true)
        @Composable
        fun GreetingPreview() {
            PlantMe_Grupo8Theme {
                Greeting("Android")
            }
        }

    }
