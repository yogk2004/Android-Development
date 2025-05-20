package com.example.weatherapp.api

data class ForecastModel(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val date_epoch: String,
    val day: Day,
    val astro: Astro,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: String,
    val maxtemp_f: String,
    val mintemp_c: String,
    val mintemp_f: String,
    val avgtemp_c: String,
    val avgtemp_f: String,
    val maxwind_kph: String,
    val maxwind_mph: String,
    val totalprecip_mm: String,
    val totalprecip_in: String,
    val avgvis_km: String,
    val avgvis_miles: String,
    val avghumidity: String,
    val condition: Condition,
    val uv: String
)

data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val moon_phase: String,
    val moon_illumination: String
)

data class Hour(
    val time_epoch: String,
    val time: String,
    val temp_c: String,
    val temp_f: String,
    val condition: Condition,
    val wind_kph: String,
    val wind_mph: String,
    val wind_dir: String,
    val pressure_mb: String,
    val pressure_in: String,
    val precip_mm: String,
    val precip_in: String,
    val humidity: String,
    val cloud: String,
    val feelslike_c: String,
    val feelslike_f: String,
    val windchill_c: String,
    val windchill_f: String,
    val heatindex_c: String,
    val heatindex_f: String,
    val dewpoint_c: String,
    val dewpoint_f: String,
    val will_it_rain: String,
    val chance_of_rain: String,
    val will_it_snow: String,
    val chance_of_snow: String,
    val vis_km: String,
    val vis_miles: String,
    val gust_kph: String,
    val gust_mph: String,
    val uv: String
)