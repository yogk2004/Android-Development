package com.yourpackage.mc_assignment_2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flight_records")
data class FlightRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Int = 0,
    val flightCode: String,
    val departureLocation: String,
    val arrivalLocation: String,
    val plannedDeparture: String,
    val plannedArrival: String,
    val realDeparture: String?,
    val realArrival: String?,
    val durationMinutes: Long
)
