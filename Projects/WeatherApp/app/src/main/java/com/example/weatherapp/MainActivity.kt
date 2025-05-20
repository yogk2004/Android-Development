package com.example.weatherapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var isCelsius by remember { mutableStateOf(true) }

            WeatherAppTheme(darkTheme = isDarkMode) {
                MainScreen(
                    viewModel = weatherViewModel,
                    isDarkMode = isDarkMode,
                    isCelsius = isCelsius,
                    onDarkModeToggle = { isDarkMode = !isDarkMode },
                    onUnitToggle = { isCelsius = !isCelsius }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WeatherViewModel,
    isDarkMode: Boolean,
    isCelsius: Boolean,
    onDarkModeToggle: () -> Unit,
    onUnitToggle: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDarkModeToggle) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                                contentDescription = "Toggle Dark Mode"
                            )
                        }
                        IconButton(onClick = onUnitToggle) {
                            Text(text = if (isCelsius) "°C" else "°F")
                        }
                        IconButton(onClick = {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = {
                            context.startActivity(Intent(context, HistoryActivity::class.java))
                        }) {
                            Icon(Icons.Filled.History, contentDescription = "History")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkMode) listOf(Color(0xFF0A0E21), Color(0xFF1D1E33))
                        else listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))
                    )
                )
        ) {
            WeatherPage(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                isCelsius = isCelsius,
                onDarkModeToggle = onDarkModeToggle,
                onUnitToggle = onUnitToggle
            )
        }
    }
}