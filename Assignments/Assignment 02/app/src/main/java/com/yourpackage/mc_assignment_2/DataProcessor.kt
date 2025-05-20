package com.yourpackage.mc_assignment_2

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataProcessor(private val storage: FlightStorage) {
    fun fetchAverageDuration(code: String, onResult: (Double?) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val avg = storage.flightAccess().computeFlightAverage(code)
            onResult(avg)
        }
    }
}