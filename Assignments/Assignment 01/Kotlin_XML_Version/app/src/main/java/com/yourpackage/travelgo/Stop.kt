package com.yourpackage.travelgo

data class Stop(
    val source: String,
    val destination: String,
    val distanceKm: Int,
    val distanceMi: Int,
    val flightTimeHrs: Double,
    val visaRequirement: String
)