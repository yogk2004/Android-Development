package com.example.weatherapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val tempC: String,
    val tempF: String,
    val condition: String,
    val iconUrl: String,
    val timestamp: Long
)