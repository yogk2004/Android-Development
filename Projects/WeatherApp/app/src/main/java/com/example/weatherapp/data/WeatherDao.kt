package com.example.weatherapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert
    suspend fun insert(weather: WeatherEntity)

    @Query("SELECT * FROM weather ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherEntity>>

    @Query("SELECT * FROM weather WHERE city = :city ORDER BY timestamp DESC LIMIT 1")
    suspend fun getWeatherByCity(city: String): WeatherEntity?
}