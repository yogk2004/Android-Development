package com.yourpackage.travelgo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourpackage.travelgo.databinding.ActivityIntroRouteJourneyBinding
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class IntroRouteJourney : AppCompatActivity() {

    private lateinit var binding: ActivityIntroRouteJourneyBinding
    private lateinit var stopsAdapter: StopsAdapter
    private val stopsData = mutableListOf<Stop>()
    private var isKm = true
    private var currentStopIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityIntroRouteJourneyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load data from StopsData.txt
        loadStopsData()

        // Set Initial and Final Journey Route
        if (stopsData.isNotEmpty()) {
            val firstStop = stopsData.first()
            val lastStop = stopsData.last()
            binding.tvJourneyRoute.text = "From: ${firstStop.source} To: ${lastStop.destination}"
        }

        // Set up RecyclerView
        stopsAdapter = StopsAdapter(stopsData)
        binding.rvStops.layoutManager = LinearLayoutManager(this)
        binding.rvStops.adapter = stopsAdapter

        // Button: Switch Units
        binding.btnSwitchUnit.setOnClickListener {
            isKm = !isKm
            stopsAdapter.switchUnit(isKm)
            binding.btnSwitchUnit.text = if (isKm) "Switch to the Miles" else "Switch to the Kilometers"
            updateProgress() // Update distance values after switching units
        }

        // Button: Next Stop
        binding.btnNextStop.setOnClickListener {
            if (currentStopIndex < stopsData.size - 1) {
                currentStopIndex++
                stopsAdapter.moveToNextStop()
                updateProgress()

                // Scroll to the newly updated stop
                binding.rvStops.smoothScrollToPosition(currentStopIndex)
            } else {
                // Reset all stops to default and mark journey complete
                stopsAdapter.resetStops()
                binding.progressBar.progress = 100
                binding.tvProgress.text = "100% Completed"
                binding.tvDistanceCovered.text = "Total Distance Covered: ${getTotalDistance(true)} ${getUnit()}"
                binding.tvDistanceLeft.text = "Total Distance Left: 0 ${getUnit()}"
                Toast.makeText(this, "Journey Completed!", Toast.LENGTH_SHORT).show()
            }
        }

        // Initial Progress Update
        updateProgress()
    }

    private fun loadStopsData() {
        val inputStream = resources.openRawResource(R.raw.stopsdata)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val jsonString = reader.use { it.readText() }
        val stopsArray = JSONArray(jsonString)

        for (i in 0 until stopsArray.length()) {
            val stop = stopsArray.getJSONObject(i)
            stopsData.add(
                Stop(
                    source = stop.getString("SOURCE"),
                    destination = stop.getString("DESTINATION"),
                    distanceKm = stop.getInt("DISTANCE_KM"),
                    distanceMi = stop.getInt("DISTANCE_MI"),
                    flightTimeHrs = stop.getDouble("FLIGHT_TIME_HRS"),
                    visaRequirement = stop.getString("VISA_REQUIREMENT")
                )
            )
        }
    }

    private fun updateProgress() {
        val progress = (currentStopIndex.toFloat() / stopsData.size * 100).toInt()
        binding.progressBar.progress = progress
        binding.tvProgress.text = "$progress% Completed"

        // Update Distance Details
        binding.tvDistanceCovered.text = "Total Distance Covered: ${getTotalDistance(true)} ${getUnit()}"
        binding.tvDistanceLeft.text = "Total Distance Left: ${getTotalDistance(false)} ${getUnit()}"
    }

    private fun getTotalDistance(covered: Boolean): Int {
        return if (covered) {
            stopsData.take(currentStopIndex).sumOf { if (isKm) it.distanceKm else it.distanceMi }
        } else {
            stopsData.drop(currentStopIndex).sumOf { if (isKm) it.distanceKm else it.distanceMi }
        }
    }

    private fun getUnit(): String {
        return if (isKm) "km" else "miles"
    }
}