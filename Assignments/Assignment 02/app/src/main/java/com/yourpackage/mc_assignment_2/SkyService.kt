package com.yourpackage.mc_assignment_2

import retrofit2.http.GET
import retrofit2.http.Query

interface SkyService {
    @GET("v1/flights")
    suspend fun retrieveFlightInfo(
        @Query("access_key") key: String,
        @Query("flight_iata") flightCode: String
    ): SkyDataResponse
}