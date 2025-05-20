package com.yourpackage.mc_assignment_2

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FlightSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val skyApi = NetworkClient.skyService
        val storage = FlightStorage.initializeStorage(applicationContext)
        val accessKey = Config.SECRET_KEY
        val flightList = listOf("IX2872", "IX235", "IX242")

        for (code in flightList) {
            try {
                Log.d("FlightSync", "Syncing $code")
                val data = skyApi.retrieveFlightInfo(accessKey, code)
                if (data.data.isNotEmpty()) { // Changed from data.flightRecords to data.data
                    val flight = data.data[0]
                    val departureTime = TimeFormatter.parseTimestamp(flight.departure.scheduled)
                    val arrivalTime = TimeFormatter.parseTimestamp(flight.arrival.scheduled)
                    val duration = (arrivalTime.time - departureTime.time) / 60000

                    storage.flightAccess().saveFlight(
                        FlightRecord(
                            flightCode = flight.flight.iata,
                            departureLocation = flight.departure.iata,
                            arrivalLocation = flight.arrival.iata,
                            plannedDeparture = flight.departure.scheduled,
                            plannedArrival = flight.arrival.scheduled,
                            realDeparture = null,
                            realArrival = null,
                            durationMinutes = duration
                        )
                    )
                    Log.d("FlightSync", "Saved ${flight.flight.iata}")
                } else {
                    Log.d("FlightSync", "No data for $code")
                }
            } catch (e: Exception) {
                Log.e("FlightSync", "Error syncing $code", e)
            }
        }
        return Result.success()
    }
}