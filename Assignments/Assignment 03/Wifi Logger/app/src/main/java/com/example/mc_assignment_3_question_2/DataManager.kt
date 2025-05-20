package com.example.mc_assignment_3_question_2

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object DataManager {
    private const val FILE_NAME = "locations.json"

    fun loadLocations(context: Context): MutableList<LocationData> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            return mutableListOf()
        }
        val json = file.readText()
        val type = object : TypeToken<MutableList<LocationData>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveLocations(context: Context, locations: List<LocationData>) {
        val json = Gson().toJson(locations)
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(json)
    }
}