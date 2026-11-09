package com.example.gpltrack.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val DEADLINE_CHANNEL_ID = "deadline_reminders"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            DEADLINE_CHANNEL_ID,
            "Promemoria Scadenze",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifiche per bollo, assicurazione e patente"
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}