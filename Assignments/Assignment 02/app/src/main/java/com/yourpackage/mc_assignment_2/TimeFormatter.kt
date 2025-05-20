package com.yourpackage.mc_assignment_2

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    fun parseTimestamp(timeString: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
        return try {
            formatter.parse(timeString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}