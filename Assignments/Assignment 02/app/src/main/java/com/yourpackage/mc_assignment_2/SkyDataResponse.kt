package com.yourpackage.mc_assignment_2

import com.google.gson.annotations.SerializedName

data class SkyDataResponse(
    @SerializedName("data")
    val data: List<SkyFlight>
)

data class SkyFlight(
    @SerializedName("flight_date")
    val flightDate: String,
    @SerializedName("flight_status")
    val flightStatus: String,
    @SerializedName("departure")
    val departure: SkyLocation,
    @SerializedName("arrival")
    val arrival: SkyLocation,
    @SerializedName("airline")
    val airline: SkyOperator,
    @SerializedName("flight")
    val flight: SkyFlightInfo,
    @SerializedName("aircraft")
    val aircraft: SkyAircraftInfo?,
    @SerializedName("live")
    val live: SkyRealTime?
)

data class SkyFlightInfo(
    @SerializedName("number")
    val number: String,
    @SerializedName("iata")
    val iata: String,
    @SerializedName("icao")
    val icao: String,
    @SerializedName("codeshared")
    val codeshared: Any?
)

data class SkyOperator(
    @SerializedName("name")
    val name: String,
    @SerializedName("iata")
    val iata: String,
    @SerializedName("icao")
    val icao: String
)

data class SkyLocation(
    @SerializedName("airport")
    val airport: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("iata")
    val iata: String,
    @SerializedName("icao")
    val icao: String,
    @SerializedName("terminal")
    val terminal: String?,
    @SerializedName("gate")
    val gate: String?,
    @SerializedName("delay")
    val delay: Int?,
    @SerializedName("scheduled")
    val scheduled: String,
    @SerializedName("estimated")
    val estimated: String,
    @SerializedName("actual")
    val actual: String?,
    @SerializedName("estimated_runway")
    val estimatedRunway: String?,
    @SerializedName("actual_runway")
    val actualRunway: String?
)

data class SkyAircraftInfo(
    @SerializedName("registration")
    val registration: String,
    @SerializedName("iata")
    val iata: String,
    @SerializedName("icao")
    val icao: String,
    @SerializedName("icao24")
    val icao24: String
)

data class SkyRealTime(
    @SerializedName("updated")
    val updated: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("altitude")
    val altitude: Double,
    @SerializedName("direction")
    val direction: Double,
    @SerializedName("speed_horizontal")
    val speedHorizontal: Double,
    @SerializedName("speed_vertical")
    val speedVertical: Double,
    @SerializedName("is_ground")
    val isGround: Boolean
)