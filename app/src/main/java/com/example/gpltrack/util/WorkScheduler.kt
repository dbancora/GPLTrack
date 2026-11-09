package com.example.gpltrack.util

// In un file WorkScheduler.kt
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleDailyDeadlineCheck(context: Context) {
    // Esegui il controllo solo quando la batteria non è scarica
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    // Crea una richiesta periodica giornaliera
    val checkRequest = PeriodicWorkRequestBuilder<DeadlineCheckWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    // Pianifica il lavoro in modo univoco (non ne crea duplicati)
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_deadline_check",
        ExistingPeriodicWorkPolicy.KEEP,
        checkRequest
    )
}