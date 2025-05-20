package com.example.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query
interface WeatherAPI {

    @GET("/v1/current.json")
    suspend fun getWeather(
        @Query("key") apikey: String,
        @Query("q") city: String
    ): retrofit2.Response<WeatherModel>

    @GET("/v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apikey: String,
        @Query("q") city: String,
        @Query("days") days: Int = 5 // Explicitly set to 5 days
    ): retrofit2.Response<ForecastModel>
}