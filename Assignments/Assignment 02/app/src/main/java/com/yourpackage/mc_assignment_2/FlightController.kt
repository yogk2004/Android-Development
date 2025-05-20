package com.yourpackage.mc_assignment_2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

class FlightController(private val display: FlightDisplay, private val skyApi: SkyService, private val storage: FlightStorage) {
    private var refreshTimer: Timer? = null

    fun initiateTracking(code: String) {
        refreshTimer?.cancel()
        refreshTimer = Timer()
        refreshTimer?.scheduleAtFixedRate(0, 20000) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        skyApi.retrieveFlightInfo(Config.SECRET_KEY, code)
                    }
                    Log.d("FlightController", "API Response: $response")
                    if (response.data.isNotEmpty()) {
                        val flight = response.data[0]
                        Log.d("FlightController", "Flight Data: $flight")
                        display.showFlightDetails(flight)
                        AlertManager(display.getActivity()).sendFlightAlert(flight)
                        storeFlightData(flight)
                    } else {
                        Log.d("FlightController", "No data for $code")
                        display.flightInfoDisplay.text = "No flight data found for $code"
                    }
                } catch (e: Exception) {
                    Log.e("FlightController", "Error fetching data: ${e.message}")
                    display.flightInfoDisplay.text = "Error: ${e.message}. Retrying in 20s."
                }
            }
        }
    }

    fun haltTracking() {
        refreshTimer?.cancel()
        refreshTimer = null
        display.resetDisplay()
    }

    private fun storeFlightData(flight: SkyFlight) {
        val departureTime = TimeFormatter.parseTimestamp(flight.departure.scheduled)
        val arrivalTime = TimeFormatter.parseTimestamp(flight.arrival.scheduled)
        val duration = (arrivalTime.time - departureTime.time) / 60000

        CoroutineScope(Dispatchers.IO).launch {
            storage.flightAccess().saveFlight(
                FlightRecord(
                    flightCode = flight.flight.iata,
                    departureLocation = flight.departure.iata,
                    arrivalLocation = flight.arrival.iata,
                    plannedDeparture = flight.departure.scheduled,
                    plannedArrival = flight.arrival.scheduled,
                    realDeparture = flight.departure.actual,
                    realArrival = flight.arrival.actual,
                    durationMinutes = duration
                )
            )
        }
    }
}