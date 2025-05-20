package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.example.weatherapp.data.WeatherEntity
import com.example.weatherapp.ui.theme.WeatherAppTheme

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        setContent {
            WeatherAppTheme {
                HistoryScreen(viewModel)
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: WeatherViewModel) {
    val weatherList = viewModel.getCachedWeather().observeAsState(emptyList())
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFA1C4FD))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Search History",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(weatherList.value) { weather ->
            WeatherHistoryItem(weather)
        }
    }
}

@Composable
fun WeatherHistoryItem(weather: WeatherEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = weather.city,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = weather.condition,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Temp: ${weather.tempC}°C / ${weather.tempF}°F",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            AsyncImage(
                model = "https:${weather.iconUrl}",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}