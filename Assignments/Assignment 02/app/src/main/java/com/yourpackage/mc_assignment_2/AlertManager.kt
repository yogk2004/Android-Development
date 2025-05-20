package com.yourpackage.mc_assignment_2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlertManager(private val ctx: Context) {
    private val alertChannel = "FlightAlerts"
    private val alertId = 202

    fun createAlertChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                alertChannel,
                "Flight Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Real-time flight alerts" }
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendFlightAlert(flight: SkyFlight) {
        val intent = Intent(ctx, CoreActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, alertChannel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Flight ${flight.flight.iata} Alert") // Changed from flightDetails to flight
            .setContentText("Status: ${determineStatus(flight.live)}") // Changed from realTime to live
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alertId, notification)
    }

    private fun determineStatus(live: SkyRealTime?): String { // Changed from realTime to live
        return live?.let {
            if (it.speedHorizontal > 0) "Airborne" else "Stationary" // Changed from groundSpeed to speedHorizontal
        } ?: "Status Unavailable"
    }
}
