package com.yourpackage.mc_assignment_2

import androidx.room.*

@Dao
interface FlightAccess {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFlight(flight: FlightRecord)

    @Query("SELECT * FROM flight_records WHERE departureLocation = :departure AND arrivalLocation = :arrival")
    suspend fun fetchFlights(departure: String, arrival: String): List<FlightRecord>

    @Query("SELECT AVG(durationMinutes) FROM flight_records WHERE departureLocation = :departure AND arrivalLocation = :arrival")
    suspend fun computeRouteAverage(departure: String, arrival: String): Double?

    @Query("SELECT AVG(durationMinutes) FROM flight_records WHERE flightCode = :flightCode")
    suspend fun computeFlightAverage(flightCode: String): Double?
}