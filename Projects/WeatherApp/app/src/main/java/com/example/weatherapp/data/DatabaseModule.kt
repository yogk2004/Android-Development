package com.example.weatherapp.data

import android.content.Context
import androidx.room.Room

object DatabaseModule {
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "weather_database"
        ).build()
    }
}