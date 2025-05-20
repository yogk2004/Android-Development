package com.yourpackage.mc_assignment_2

import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class FlightDisplay(private val activity: CoreActivity) {
    val beginTrackButton: Button = activity.findViewById(R.id.trackButton)
    val endTrackButton: Button = activity.findViewById(R.id.stopButton)
    val flightCodeField: EditText = activity.findViewById(R.id.flightCodeEdit)
    val flightInfoDisplay: TextView = activity.findViewById(R.id.flightInfoText)
    val liveInfoDisplay: TextView = activity.findViewById(R.id.liveDataText)
    val averageCodeField: EditText = activity.findViewById(R.id.averageCodeEdit)
    val averageResultDisplay: TextView = activity.findViewById(R.id.averageTimeText)
    val calcAverageButton: Button = activity.findViewById(R.id.computeAverageButton)
    val flightRadarWebView: WebView = activity.findViewById(R.id.flightRadarWebView)

    init {
        // Enable JavaScript for the WebView since FlightRadar24 likely requires it
        flightRadarWebView.settings.javaScriptEnabled = true
    }

    fun setupDisplay() {
        endTrackButton.visibility = View.GONE
        flightCodeField.hint = "Flight Code (e.g., AI281)"
        flightInfoDisplay.text = "Awaiting flight data"
        liveInfoDisplay.text = ""
        averageResultDisplay.text = ""
        flightRadarWebView.visibility = View.GONE
    }

    fun fetchFlightCode(): String {
        return flightCodeField.text.toString().replace("\\s".toRegex(), "").uppercase()
    }

    fun switchButtonState(isActive: Boolean) {
        beginTrackButton.isEnabled = !isActive
        endTrackButton.visibility = if (isActive) View.VISIBLE else View.GONE
    }

    fun resetDisplay() {
        flightInfoDisplay.text = "Tracking paused"
        liveInfoDisplay.text = ""
        flightRadarWebView.visibility = View.GONE
    }

    fun showFlightDetails(flight: SkyFlight) {
        flightInfoDisplay.text = """
            Flight: ${flight.flight.iata}
            Status: ${flight.flightStatus}
            Airline: ${flight.airline.name}
            Departure: ${flight.departure.airport} (${flight.departure.iata})
            Scheduled: ${flight.departure.scheduled}
            Actual: ${flight.departure.actual ?: "N/A"}
            Arrival: ${flight.arrival.airport} (${flight.arrival.iata})
            Scheduled: ${flight.arrival.scheduled}
            Actual: ${flight.arrival.actual ?: "N/A"}
        """.trimIndent()

        liveInfoDisplay.text = flight.live?.let {
            """
            Live Tracking:
            Latitude: ${it.latitude}
            Longitude: ${it.longitude}
            Altitude: ${it.altitude}m
            Speed: ${it.speedHorizontal}km/h
            Updated: ${it.updated}
            """.trimIndent()
        } ?: "Live data not available"
    }

    fun showAverageDuration(code: String, duration: Double?) {
        averageResultDisplay.text = duration?.let {
            "Avg duration for $code: ${String.format("%.2f", it)} min"
        } ?: "No duration data for $code"
    }

    fun loadFlightRadar(flightCode: String) {
        val url = "https://www.flightradar24.com/data/flights/${flightCode.lowercase()}"
        flightRadarWebView.loadUrl(url)
        flightRadarWebView.visibility = View.VISIBLE
    }

    fun getActivity(): CoreActivity {
        return activity
    }
}