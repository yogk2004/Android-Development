package com.yourpackage.mc_assignment_2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    val skyService: SkyService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aviationstack.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SkyService::class.java)
    }
}