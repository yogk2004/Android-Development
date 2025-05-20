package com.yourpackage.mc_assignment_2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class CoreActivity : AppCompatActivity() {
    private lateinit var flightDisplay: FlightDisplay
    private lateinit var flightController: FlightController
    private lateinit var dataProcessor: DataProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val storage = FlightStorage.initializeStorage(this)
        val skyApi = NetworkClient.skyService
        flightDisplay = FlightDisplay(this)
        flightController = FlightController(flightDisplay, skyApi, storage)
        dataProcessor = DataProcessor(storage)

        flightDisplay.setupDisplay()

        scheduleDataSync()

        AlertManager(this).createAlertChannel()

        flightDisplay.beginTrackButton.setOnClickListener {
            val code = flightDisplay.fetchFlightCode()
            if (InputValidator.checkFlightCode(code)) {
                flightController.initiateTracking(code)
                flightDisplay.switchButtonState(true)
                flightDisplay.loadFlightRadar(code) // Load the WebView with the flight radar URL
            } else {
                notifyUser("Invalid code (e.g., AI281)")
            }
        }

        flightDisplay.endTrackButton.setOnClickListener {
            flightController.haltTracking()
            flightDisplay.switchButtonState(false)
        }

        flightDisplay.calcAverageButton.setOnClickListener {
            val code = flightDisplay.averageCodeField.text.toString().trim().uppercase()
            if (InputValidator.checkFlightCode(code)) {
                dataProcessor.fetchAverageDuration(code) { duration ->
                    flightDisplay.showAverageDuration(code, duration)
                }
            } else {
                notifyUser("Invalid flight code")
            }
        }
    }

    private fun scheduleDataSync() {
        val syncRequest = PeriodicWorkRequestBuilder<FlightSyncWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FlightSyncTask",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        flightController.haltTracking()
    }
}