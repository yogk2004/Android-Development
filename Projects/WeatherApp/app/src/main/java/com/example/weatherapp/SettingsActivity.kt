package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.work.WeatherWorker
import java.util.concurrent.TimeUnit

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            WeatherAppTheme(darkTheme = isDarkMode) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeToggle: () -> Unit
) {
    var defaultCity by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val textColor = if (isDarkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = textColor) },
                actions = {
                    IconButton(onClick = onDarkModeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                            contentDescription = "Toggle Dark Mode",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = textColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (isDarkMode) listOf(Color(0xFF0A0E21), Color(0xFF1D1E33))
                        else listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp), // Reduced padding to move content up
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = defaultCity,
                    onValueChange = { defaultCity = it },
                    label = { Text("Optional City for Background Refresh (Leave blank for location)", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        cursorColor = textColor,
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.5f),
                        focusedTextColor = textColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val data = workDataOf("city" to (defaultCity.ifEmpty { null }))
                        val workRequest = PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.HOURS)
                            .setInputData(data)
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()
                        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                            "weather_refresh",
                            ExistingPeriodicWorkPolicy.REPLACE,
                            workRequest
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Save and Start Background Refresh")
                }
            }
        }
    }
}