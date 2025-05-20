package com.yourpackage.mc_assignment_2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FlightRecord::class], version = 1)
abstract class FlightStorage : RoomDatabase() {
    abstract fun flightAccess(): FlightAccess

    companion object {
        @Volatile
        private var storageInstance: FlightStorage? = null

        fun initializeStorage(context: Context): FlightStorage {
            return storageInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlightStorage::class.java,
                    "flight_records_db"
                ).build()
                storageInstance = instance
                instance
            }
        }
    }
}