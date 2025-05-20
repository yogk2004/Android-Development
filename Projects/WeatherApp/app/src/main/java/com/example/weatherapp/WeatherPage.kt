package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.font.FontStyle
import com.airbnb.lottie.compose.*
import com.example.weatherapp.api.ForecastModel
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.WeatherModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherPage(
    viewModel: WeatherViewModel,
    isDarkMode: Boolean,
    isCelsius: Boolean,
    onDarkModeToggle: () -> Unit,
    onUnitToggle: () -> Unit
) {
    var city by remember { mutableStateOf("") }
    val weatherResult = viewModel.weatherResult.observeAsState()
    val forecastResult = viewModel.forecastResult.observeAsState()
    val textColor = if (isDarkMode) Color.White else Color(0xFF333333)
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember { context.getSystemService(LocationManager::class.java) }

    // Text-to-Speech initialization
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.getDefault()
                ttsInitialized = true
            } else {
                Toast.makeText(context, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    // Speak weather data when it loads
    LaunchedEffect(weatherResult.value) {
        if (ttsInitialized && weatherResult.value is NetworkResponse.Success) {
            val weatherData = (weatherResult.value as NetworkResponse.Success).data
            if (weatherData != null) {
                val speechText = buildWeatherSpeechText(weatherData, isCelsius)
                tts.value?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    // Lottie animation states
    val initialAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation))
    val backgroundAnimation by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.background))
    val lottieAnimatable = rememberLottieAnimatable()
    val isDataLoaded = weatherResult.value is NetworkResponse.Success

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.firstOrNull()?.let { spokenText ->
                city = spokenText
                viewModel.getData(spokenText)
            } ?: Toast.makeText(context, "No speech recognized", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for RECORD_AUDIO
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the city name")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        Log.d("WeatherPage", "Fine location granted: $fineLocationGranted, Coarse location granted: $coarseLocationGranted")

        if (fineLocationGranted || coarseLocationGranted) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.d("WeatherPage", "Location services enabled, requesting location")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d("WeatherPage", "Last location retrieved: ${location.latitude}, ${location.longitude}")
                        city = "Your Location"
                        viewModel.getDataByCoordinates(location.latitude.toString(), location.longitude)
                    } else {
                        Log.w("WeatherPage", "Last location is null, requesting new location")
                        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                            interval = 10000
                            fastestInterval = 5000
                        }
                        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                result.lastLocation?.let { freshLocation ->
                                    Log.d("WeatherPage", "Fresh location retrieved: ${freshLocation.latitude}, ${freshLocation.longitude}")
                                    city = "Your Location"
                                    viewModel.getDataByCoordinates(freshLocation.latitude.toString(), freshLocation.longitude)
                                } ?: run {
                                    Log.e("WeatherPage", "No location received from update")
                                    Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                                }
                                fusedLocationClient.removeLocationUpdates(this)
                            }
                        }
                        try {
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                        } catch (e: SecurityException) {
                            Log.e("WeatherPage", "Location permission error: ${e.message}")
                            Toast.makeText(context, "Location permission error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("WeatherPage", "Location fetch failed: ${exception.message}")
                    Toast.makeText(context, "Location fetch failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w("WeatherPage", "Location services disabled")
                Toast.makeText(context, "Please enable location services", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        } else {
            Log.w("WeatherPage", "Location permission denied")
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) listOf(Color(0xFF0A0E21), Color(0xFF1D1E33))
                    else listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))
                )
            )
    ) {
        if (isDataLoaded) {
            LottieAnimation(
                composition = backgroundAnimation,
                progress = { lottieAnimatable.progress },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            LaunchedEffect(isDataLoaded) {
                lottieAnimatable.animate(
                    composition = backgroundAnimation,
                    iterations = LottieConstants.IterateForever
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Search for Location", color = textColor) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        cursorColor = textColor,
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.5f),
                        focusedTextColor = textColor
                    ),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { viewModel.getData(city) }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor)
                            }
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED) {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the city name")
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = textColor)
                            }
                        }
                    },
                    leadingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Menu", tint = textColor)
                        }
                    }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(if (isDarkMode) Color(0xFF1D1E33) else Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Your Location", color = textColor) },
                        onClick = {
                            expanded = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = "Your Location", tint = textColor)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isDataLoaded) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        composition = initialAnimation,
                        progress = { lottieAnimatable.progress },
                        modifier = Modifier.size(400.dp),
                        contentScale = ContentScale.Fit
                    )
                    LaunchedEffect(initialAnimation) {
                        initialAnimation?.let {
                            lottieAnimatable.animate(
                                composition = it,
                                iterations = LottieConstants.IterateForever
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "WEATHER APP",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                }
            }

            when (val res = weatherResult.value) {
                is NetworkResponse.Error -> {
                    Text(
                        text = res.message,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                NetworkResponse.Loading -> {
                    CircularProgressIndicator(color = textColor)
                }
                is NetworkResponse.Success -> {
                    res.data?.let { weatherData ->
                        val forecastData = forecastResult.value
                        WeatherScreen(
                            weatherData = weatherData,
                            forecastData = forecastData,
                            isCelsius = isCelsius,
                            isDarkMode = isDarkMode
                        )
                    } ?: Text(
                        "No data available",
                        color = textColor
                    )
                }
                null -> {}
            }
        }
    }
}

// Helper function to build the speech text for weather data
private fun buildWeatherSpeechText(weatherData: WeatherModel, isCelsius: Boolean): String {
    val location = "${weatherData.location.name}, ${weatherData.location.country}"
    val temperature = if (isCelsius) "${weatherData.current.temp_c} degrees Celsius" else "${weatherData.current.temp_f} degrees Fahrenheit"
    val condition = weatherData.current.condition.text
    val feelsLike = if (isCelsius) "${weatherData.current.feelslike_c} degrees Celsius" else "${weatherData.current.feelslike_f} degrees Fahrenheit"
    val windSpeed = "${weatherData.current.wind_kph} kilometers per hour, direction ${weatherData.current.wind_dir}"
    val humidity = "${weatherData.current.humidity} percent"
    val pressure = "${weatherData.current.pressure_mb} hectopascals"
    val uvIndex = weatherData.current.uv
    val visibility = "${weatherData.current.vis_km} kilometers"

    return """
        Weather in $location: $temperature, $condition. 
        Feels like $feelsLike. 
        Wind speed is $windSpeed. 
        Humidity is $humidity. 
        Pressure is $pressure. 
        UV index is $uvIndex. 
        Visibility is $visibility.
    """.trimIndent().replace("\n", ", ")
}

@Composable
fun WeatherScreen(
    weatherData: WeatherModel,
    forecastData: NetworkResponse<ForecastModel>?,
    isCelsius: Boolean,
    isDarkMode: Boolean
) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF333333)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${weatherData.location.name}, ${weatherData.location.country}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${weatherData.location.localtime}",
                            fontSize = 16.sp,
                            color = textColor.copy(alpha = 0.8f),
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 1000)) + scaleIn(
                        animationSpec = tween(durationMillis = 1000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val iconUrl = "https:${weatherData.current.condition.icon.replace("64x64", "128x128")}"
                        Image(
                            painter = rememberAsyncImagePainter(iconUrl),
                            contentDescription = "Weather Icon",
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isCelsius) "${weatherData.current.temp_c}°C" else "${weatherData.current.temp_f}°F",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = weatherData.current.condition.text,
                            fontSize = 20.sp,
                            color = textColor.copy(alpha = 0.8f),
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(durationMillis = 1000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WeatherInfoRow(
                            label = "Feels Like",
                            value = if (isCelsius) "${weatherData.current.feelslike_c}°C" else "${weatherData.current.feelslike_f}°F",
                            icon = { Text("🌡️", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )
                        WeatherInfoRow(
                            label = "Wind Speed",
                            value = "${weatherData.current.wind_kph} km/h (${weatherData.current.wind_dir})",
                            icon = { Text("💨", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )
                        WeatherInfoRow(
                            label = "Humidity",
                            value = "${weatherData.current.humidity}%",
                            icon = { Text("💧", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )
                        WeatherInfoRow(
                            label = "Pressure",
                            value = "${weatherData.current.pressure_mb} hPa",
                            icon = { Text("📊", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )
                        WeatherInfoRow(
                            label = "UV Index",
                            value = weatherData.current.uv,
                            icon = { Text("☀️", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )
                        WeatherInfoRow(
                            label = "Visibility",
                            value = "${weatherData.current.vis_km} km",
                            icon = { Text("👀", fontSize = 24.sp, color = textColor) },
                            textColor = textColor
                        )

                        // Hourly Forecast Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.elevatedCardElevation(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x80FFFFFF),
                                                Color(0x40FFFFFF)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "24-Hour Forecast",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                        Text(
                                            text = "Scroll to explore",
                                            fontSize = 14.sp,
                                            color = textColor.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    when (val res = forecastData) {
                                        is NetworkResponse.Success -> {
                                            res.data?.let { forecast ->
                                                HourlyForecast(
                                                    forecast = forecast,
                                                    isCelsius = isCelsius,
                                                    textColor = textColor
                                                )
                                            } ?: Text(
                                                "No hourly forecast data available",
                                                color = textColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                        is NetworkResponse.Error -> {
                                            Text(
                                                text = res.message,
                                                color = Color.Red,
                                                fontSize = 14.sp
                                            )
                                        }
                                        NetworkResponse.Loading -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = textColor
                                            )
                                        }
                                        null -> {}
                                    }
                                }
                            }
                        }

                        // 5-Day Forecast Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.elevatedCardElevation(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0x80FFFFFF),
                                                Color(0x40FFFFFF)
                                            )
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "5-Day Forecast",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                        Text(
                                            text = "Scroll to explore",
                                            fontSize = 14.sp,
                                            color = textColor.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    when (val res = forecastData) {
                                        is NetworkResponse.Success -> {
                                            res.data?.let { forecast ->
                                                FiveDayForecast(
                                                    forecast = forecast,
                                                    isCelsius = isCelsius,
                                                    textColor = textColor
                                                )
                                            } ?: Text(
                                                "No forecast data available",
                                                color = textColor,
                                                fontSize = 14.sp
                                            )
                                        }
                                        is NetworkResponse.Error -> {
                                            Text(
                                                text = res.message,
                                                color = Color.Red,
                                                fontSize = 14.sp
                                            )
                                        }
                                        NetworkResponse.Loading -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = textColor
                                            )
                                        }
                                        null -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyForecast(
    forecast: ForecastModel,
    isCelsius: Boolean,
    textColor: Color
) {
    val todayForecast = forecast.forecast.forecastday.getOrNull(1)
    val tomorrowForecast = forecast.forecast.forecastday.getOrNull(2)
    val hourlyDataToday = todayForecast?.hour ?: emptyList()
    val hourlyDataTomorrow = tomorrowForecast?.hour ?: emptyList()
    val hourlyData = hourlyDataToday + hourlyDataTomorrow
    val scrollState = rememberScrollState()

    val currentTime = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val sdfHour = SimpleDateFormat("HH", Locale.getDefault())
    val currentHour = sdfHour.format(currentTime.time).toIntOrNull() ?: 0

    val startTime = Calendar.getInstance().apply {
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val endTime = Calendar.getInstance().apply {
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.HOUR_OF_DAY, 24)
    }.time

    val filteredHourlyData = hourlyData.filter { hour ->
        val hourTime = sdf.parse(hour.time)
        hourTime != null && hourTime >= startTime && hourTime <= endTime
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        filteredHourlyData.forEach { hour ->
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(hour.time) ?: Date()
            )
            val temp = if (isCelsius) hour.temp_c else hour.temp_f
            val windSpeed = hour.wind_kph

            Column(
                modifier = Modifier
                    .width(80.dp)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = time,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.SansSerif
                )
                Image(
                    painter = rememberAsyncImagePainter("https:${hour.condition.icon}"),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "$temp°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = FontFamily.SansSerif
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💨",
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$windSpeed km/h",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
fun FiveDayForecast(
    forecast: ForecastModel,
    isCelsius: Boolean,
    textColor: Color
) {
    val forecastDays = forecast.forecast.forecastday
    val daysToShow = forecastDays.take(5).map { it.date }
    val scrollState = rememberScrollState()

    LaunchedEffect(forecastDays) {
        println("Number of forecast days received: ${forecastDays.size}")
    }

    val temps = forecastDays.flatMap {
        listOf(
            (if (isCelsius) it.day.maxtemp_c.toFloatOrNull() else it.day.maxtemp_f.toFloatOrNull()) ?: 0f,
            (if (isCelsius) it.day.mintemp_c.toFloatOrNull() else it.day.mintemp_f.toFloatOrNull()) ?: 0f
        )
    }
    val minTemp = temps.minOrNull() ?: 0f
    val maxTemp = temps.maxOrNull() ?: 0f
    val tempRange = if (maxTemp != minTemp) maxTemp - minTemp else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        forecastDays.take(5).forEachIndexed { index, day ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(day.date)
            val dayLabel = when (index) {
                0 -> "Yesterday"
                1 -> "Today"
                2 -> "Tomorrow"
                else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date ?: Date())
            }

            val maxTempValue = if (isCelsius) day.day.maxtemp_c else day.day.maxtemp_f
            val minTempValue = if (isCelsius) day.day.mintemp_c else day.day.mintemp_f
            val windSpeed = day.day.maxwind_kph

            Column(
                modifier = Modifier
                    .width(100.dp)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayLabel,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.SansSerif
                )
                Image(
                    painter = rememberAsyncImagePainter("https:${day.day.condition.icon}"),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = "$maxTempValue°",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = FontFamily.SansSerif
                )
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val totalDays = daysToShow.size
                        val pointSpacing = width * totalDays / (totalDays - 1)

                        val maxPoints = daysToShow.mapIndexed { i, _ ->
                            val temp = (if (isCelsius) forecastDays[i].day.maxtemp_c.toFloatOrNull() else forecastDays[i].day.maxtemp_f.toFloatOrNull()) ?: 0f
                            val normalized = (temp - minTemp) / tempRange
                            val x = (i * pointSpacing / totalDays).toFloat()
                            val y = height * (1 - normalized)
                            Offset(x, y)
                        }

                        val minPoints = daysToShow.mapIndexed { i, _ ->
                            val temp = (if (isCelsius) forecastDays[i].day.mintemp_c.toFloatOrNull() else forecastDays[i].day.mintemp_f.toFloatOrNull()) ?: 0f
                            val normalized = (temp - minTemp) / tempRange
                            val x = (i * pointSpacing / totalDays).toFloat()
                            val y = height * (1 - normalized)
                            Offset(x, y)
                        }

                        val currentX = (index * pointSpacing / totalDays).toFloat()

                        val maxPath = Path().apply {
                            maxPoints.forEachIndexed { i, point ->
                                if (i == 0) moveTo(point.x, point.y)
                                else lineTo(point.x, point.y)
                            }
                        }
                        drawPath(
                            path = maxPath,
                            color = Color(0xFFFFA500),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )

                        val minPath = Path().apply {
                            minPoints.forEachIndexed { i, point ->
                                if (i == 0) moveTo(point.x, point.y)
                                else lineTo(point.x, point.y)
                            }
                        }
                        drawPath(
                            path = minPath,
                            color = Color(0xFF00BFFF),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )

                        if (index < maxPoints.size) {
                            drawCircle(
                                color = Color(0xFFFFA500),
                                radius = 4.dp.toPx(),
                                center = Offset(currentX, maxPoints[index].y)
                            )
                            drawCircle(
                                color = Color(0xFF00BFFF),
                                radius = 4.dp.toPx(),
                                center = Offset(currentX, minPoints[index].y)
                            )
                        }
                    }
                }
                Text(
                    text = "$minTempValue°",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f),
                    fontFamily = FontFamily.SansSerif
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💨",
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$windSpeed km/h",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f),
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherInfoRow(label: String, value: String, icon: @Composable () -> Unit, textColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x80FFFFFF),
                            Color(0x40FFFFFF)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(textColor.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}