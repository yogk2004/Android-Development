package com.yourpackage.kotlin_compose_version

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelApp()
        }
    }
}

@Composable
fun TravelApp() {
    val context = LocalContext.current
    val stops = remember { loadStopsFromFile(context) }
    var currentStopIndex by remember { mutableStateOf(0) }
    var useMiles by remember { mutableStateOf(false) }

    // Create a LazyListState for controlling the LazyColumn's scroll position.
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val totalDistance = stops.sumOf { if (useMiles) it.distanceMi else it.distanceKm }
    val coveredDistance = stops.take(currentStopIndex).sumOf { if (useMiles) it.distanceMi else it.distanceKm }
    val remainingDistance = totalDistance - coveredDistance
    val progress = if (totalDistance > 0) coveredDistance.toFloat() / totalDistance else 0f

    // Whenever the currentStopIndex changes, scroll to that item.
    LaunchedEffect(currentStopIndex) {
        if (currentStopIndex < stops.size) {
            listState.animateScrollToItem(currentStopIndex)
        }
    }

    Scaffold(
        containerColor = Color.Black, // App background is black.
        contentColor = Color.White,   // Default content color is white.
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Journey Details", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "From: ${stops.firstOrNull()?.source} To: ${stops.lastOrNull()?.destination}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Current stop text.
            Text(
                "Current Stop: ${
                    if (currentStopIndex < stops.size) stops[currentStopIndex].source else "Journey Completed"
                }",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Use LazyColumn with the list state for scrolling.
            if (stops.size > 3) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(stops) { index, stop ->
                        StopItem(
                            stop = stop,
                            useMiles = useMiles,
                            // Highlight the stop if its index equals currentStopIndex (and journey not complete).
                            isNextStop = (currentStopIndex < stops.size && index == currentStopIndex)
                        )
                    }
                }
            } else {
                // For a small list, simply iterate with indices.
                stops.forEachIndexed { index, stop ->
                    StopItem(
                        stop = stop,
                        useMiles = useMiles,
                        isNextStop = (currentStopIndex < stops.size && index == currentStopIndex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Progress indicator with Dodger Blue color.
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E90FF)
            )
            // Display completion percentage.
            Text(
                "Completion: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                "Distance Covered: ${formatDistance(coveredDistance, useMiles)} | Remaining: ${formatDistance(remainingDistance, useMiles)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row with two buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { useMiles = !useMiles },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E90FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Switch to the ${if (useMiles) "KM" else "Miles"}")
                }
                Button(
                    onClick = {
                        if (currentStopIndex < stops.size) {
                            currentStopIndex++
                        }
                    },
                    enabled = currentStopIndex < stops.size,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E90FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Next Stop Reached")
                }
            }
        }
    }
}

@Composable
fun StopItem(stop: Stop, useMiles: Boolean, isNextStop: Boolean) {
    // Default card color is now 0xFF1E90FF.
    // If this stop is the next stop, change its color to #87CEFA.
    val cardColor = if (isNextStop) Color(0xFF87CEFA) else Color(0xFF1E90FF)
    // We use white text for both since the app theme is dark.
    val textColor = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Bold stop names.
            Text(
                "From: ${stop.source} To: ${stop.destination}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
            // Distance text.
            Text(
                "Distance: ${formatDistance(if (useMiles) stop.distanceMi else stop.distanceKm, useMiles)}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            // Flight time text.
            Text(
                "Flight Time: ${stop.flightTimeHrs} hrs",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            // Visa requirement in Yellow.
            Text(
                "Visa Requirement: ${stop.visaRequirement}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Yellow
            )
        }
    }
}

data class Stop(
    val source: String,
    val destination: String,
    val distanceKm: Int,
    val distanceMi: Int,
    val flightTimeHrs: Double,
    val visaRequirement: String
)

fun loadStopsFromFile(context: Context): List<Stop> {
    val stops = mutableListOf<Stop>()
    try {
        val inputStream = context.resources.openRawResource(R.raw.stopdata)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonText = reader.use { it.readText() }
        val jsonArray = JSONArray(jsonText)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            stops.add(
                Stop(
                    obj.getString("SOURCE"),
                    obj.getString("DESTINATION"),
                    obj.getInt("DISTANCE_KM"),
                    obj.getInt("DISTANCE_MI"),
                    obj.getDouble("FLIGHT_TIME_HRS"),
                    obj.getString("VISA_REQUIREMENT")
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return stops
}

fun formatDistance(distance: Int, useMiles: Boolean): String {
    return if (useMiles) "$distance miles" else "$distance km"
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TravelApp()
}