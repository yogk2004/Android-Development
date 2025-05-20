package com.example.weatherapp.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.MainActivity
import com.example.weatherapp.api.Constants
import com.example.weatherapp.api.RetrofitInstance
import com.example.weatherapp.data.DatabaseModule
import com.example.weatherapp.data.WeatherEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("MissingPermission") // Suppresses the lint warning if permissions are checked
class WeatherWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(appContext)
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Check for location permissions
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasFineLocationPermission && !hasCoarseLocationPermission) {
                    // Log or debug the reason for retry
                    println("Location permissions not granted, retrying...")
                    return@withContext Result.retry()
                }

                // Get the last known location using suspendCancellableCoroutine
                val location = try {
                    getLastLocation()
                } catch (e: SecurityException) {
                    println("SecurityException: Location access denied, retrying...")
                    return@withContext Result.retry()
                }
                if (location != null) {
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude
                    val weatherApi = RetrofitInstance.weatherAPI
                    val response = weatherApi.getWeather(Constants.apikey, "$latitude,$longitude")
                    if (response.isSuccessful) {
                        response.body()?.let { weatherModel ->
                            // Cache the weather data
                            val database = DatabaseModule.provideDatabase(applicationContext)
                            database.weatherDao().insert(
                                WeatherEntity(
                                    city = weatherModel.location.name,
                                    tempC = weatherModel.current.temp_c,
                                    tempF = weatherModel.current.temp_f,
                                    condition = weatherModel.current.condition.text,
                                    iconUrl = weatherModel.current.condition.icon,
                                    timestamp = System.currentTimeMillis()
                                )
                            )

                            // Send notification
                            sendWeatherNotification(weatherModel.location.name, weatherModel.current.temp_c)
                        }
                        Result.success()
                    } else {
                        Result.retry()
                    }
                } else {
                    println("Location unavailable, retrying...")
                    Result.retry() // Retry if location is unavailable
                }
            } catch (e: Exception) {
                println("Exception in doWork: ${e.message}, retrying...")
                Result.retry()
            }
        }
    }

    // Suspend function to get last location
    private suspend fun getLastLocation(): android.location.Location? = suspendCancellableCoroutine { continuation ->
        val task: Task<android.location.Location> = fusedLocationClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                continuation.resume(location)
            } else {
                continuation.resume(null)
            }
        }.addOnFailureListener { exception ->
            if (exception is SecurityException) {
                continuation.resumeWithException(exception)
            } else {
                continuation.resume(null)
            }
        }
    }

    private fun sendWeatherNotification(city: String, tempC: String) {
        val channelId = "weather_updates"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Weather Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for weather updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app when notification is clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Weather Update for $city")
            .setContentText("Current temperature: $tempC°C")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}