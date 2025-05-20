package com.example.weatherapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.Constants
import com.example.weatherapp.api.ForecastModel
import com.example.weatherapp.api.NetworkResponse
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.api.WeatherModel
import com.example.weatherapp.data.AppDatabase
import com.example.weatherapp.data.DatabaseModule
import com.example.weatherapp.data.WeatherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val weatherApi = RetrofitInstance.weatherAPI
    private val _weatherResult = MutableLiveData<NetworkResponse<WeatherModel>>()
    val weatherResult: LiveData<NetworkResponse<WeatherModel>> = _weatherResult

    private val _forecastResult = MutableLiveData<NetworkResponse<ForecastModel>>()
    val forecastResult: LiveData<NetworkResponse<ForecastModel>> = _forecastResult

    private val database: AppDatabase = DatabaseModule.provideDatabase(application)
    private val weatherDao = database.weatherDao()

    // Fetch weather data by city name
    fun getData(city: String) {
        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                // Check cache first
                val cachedWeather = weatherDao.getWeatherByCity(city)
                if (cachedWeather != null && isCacheValid(cachedWeather.timestamp)) {
                    _weatherResult.value = NetworkResponse.Success(
                        WeatherModel(
                            current = com.example.weatherapp.api.Current(
                                cloud = "",
                                condition = com.example.weatherapp.api.Condition(
                                    code = "",
                                    icon = cachedWeather.iconUrl,
                                    text = cachedWeather.condition
                                ),
                                dewpoString_c = "",
                                dewpoString_f = "",
                                feelslike_c = "",
                                feelslike_f = "",
                                gust_kph = "",
                                gust_mph = "",
                                heatindex_c = "",
                                heatindex_f = "",
                                humidity = "",
                                is_day = "",
                                last_updated = "",
                                last_updated_epoch = "",
                                precip_in = "",
                                precip_mm = "",
                                pressure_in = "",
                                pressure_mb = "",
                                temp_c = cachedWeather.tempC,
                                temp_f = cachedWeather.tempF,
                                uv = "",
                                vis_km = "",
                                vis_miles = "",
                                wind_degree = "",
                                wind_dir = "",
                                wind_kph = "",
                                wind_mph = "",
                                windchill_c = "",
                                windchill_f = ""
                            ),
                            location = com.example.weatherapp.api.Location(
                                country = "",
                                lat = "",
                                localtime = "",
                                localtime_epoch = "",
                                lon = "",
                                name = city,
                                region = "",
                                tz_id = ""
                            )
                        )
                    )
                } else {
                    // Fetch from network
                    fetchWeatherFromNetwork(city)
                    fetchForecastFromNetwork(city)
                }
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Failed to load: ${e.message}")
                _forecastResult.value = NetworkResponse.Error("Failed to load: ${e.message}")
            }
        }
    }

    // Fetch weather data by coordinates
    fun getDataByCoordinates(latitude: String, longitude: Double) {
        _weatherResult.value = NetworkResponse.Loading
        _forecastResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val query = "$latitude,$longitude"
                fetchWeatherFromNetwork(query)
                fetchForecastFromNetwork(query)
            } catch (e: Exception) {
                _weatherResult.value = NetworkResponse.Error("Failed to load: ${e.message}")
                _forecastResult.value = NetworkResponse.Error("Failed to load: ${e.message}")
            }
        }
    }

    private suspend fun fetchWeatherFromNetwork(query: String) {
        val weatherResponse = weatherApi.getWeather(Constants.apikey, query)
        if (weatherResponse.isSuccessful) {
            Log.i("Weather Response:", weatherResponse.body().toString())
            weatherResponse.body()?.let { weatherModel ->
                _weatherResult.value = NetworkResponse.Success(weatherModel)
                // Cache the data
                withContext(Dispatchers.IO) {
                    weatherDao.insert(
                        WeatherEntity(
                            city = weatherModel.location.name,
                            tempC = weatherModel.current.temp_c,
                            tempF = weatherModel.current.temp_f,
                            condition = weatherModel.current.condition.text,
                            iconUrl = weatherModel.current.condition.icon,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        } else {
            Log.i("Weather Error:", weatherResponse.message())
            _weatherResult.value = NetworkResponse.Error("Failed to load weather")
        }
    }

    private suspend fun fetchForecastFromNetwork(query: String) {
        val forecastResponse = weatherApi.getForecast(Constants.apikey, query)
        if (forecastResponse.isSuccessful) {
            Log.i("Forecast Response:", forecastResponse.body().toString())
            forecastResponse.body()?.let {
                _forecastResult.value = NetworkResponse.Success(it)
            }
        } else {
            Log.i("Forecast Error:", forecastResponse.message())
            _forecastResult.value = NetworkResponse.Error("Failed to load forecast")
        }
    }

    // Check if cached data is still valid (e.g., within 1 hour)
    private fun isCacheValid(timestamp: Long): Boolean {
        val cacheDuration = 60 * 60 * 1000 // 1 hour in milliseconds
        return System.currentTimeMillis() - timestamp < cacheDuration
    }

    // Expose cached weather data for HistoryActivity
    fun getCachedWeather(): LiveData<List<WeatherEntity>> {
        return weatherDao.getAllWeather().asLiveData()
    }
}