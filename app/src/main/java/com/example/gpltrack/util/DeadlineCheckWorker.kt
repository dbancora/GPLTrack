package com.example.gpltrack.util

// In un file DeadlineCheckWorker.kt
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gpltrack.R
import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.util.nextDeadline
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DeadlineCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Leggiamo il profilo direttamente dalle SharedPreferences
        val prefs = applicationContext.getSharedPreferences("duster_profile", Context.MODE_PRIVATE)
        val rawProfile = prefs.getString("profile", null) ?: return Result.success()

        val profileJson = org.json.JSONObject(rawProfile)

        // Estraiamo le date di scadenza (se esistono)
        val bollo = if (profileJson.has("bollo")) profileJson.getLong("bollo") else null
        val insurance = if (profileJson.has("insurance")) profileJson.getLong("insurance") else null
        val licence = if (profileJson.has("licencePlate")) profileJson.getLong("licencePlate") else null

        // Controlliamo le scadenze una per una
        checkDeadline(bollo, "Bollo Auto")
        checkDeadline(insurance, "Assicurazione")
        checkDeadline(licence, "Patente")

        return Result.success()
    }

    private fun checkDeadline(epochDay: Long?, title: String) {
        if (epochDay == null) return

        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(),
            java.time.LocalDate.ofEpochDay(epochDay)
        )

        // Notifica se scade tra 0 e 7 giorni
        if (daysUntil in 0..7) {
            val message = if (daysUntil == 0L) "Scade oggi!" else "Mancano $daysUntil giorni."
            showNotification(title, message)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, DEADLINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usa un'icona adatta
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(1, notification)
    }
}