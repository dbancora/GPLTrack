package com.example.gpltrack.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

internal val dateFmt: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

internal fun fmtDate(d: LocalDate): String = d.format(dateFmt)
internal fun fmtKm(km: Int): String = String.format(Locale.ITALY, "%,d km", km)
internal fun fmtLiters(l: Double): String = String.format(Locale.ITALY, "%.2f L", l)
internal fun fmtEuro(e: Double): String = String.format(Locale.ITALY, "%.2f €", e)
internal fun fmtPrice(e: Double): String = String.format(Locale.ITALY, "%.3f €/L", e)
internal fun fmtNumKm(km: Int): String = String.format(Locale.ITALY, "%,d", km)
internal fun fmtNumL(l: Double): String = String.format(Locale.ITALY, "%.2f", l)
internal fun fmtNumPrice(e: Double): String = String.format(Locale.ITALY, "%.3f", e)
internal fun fmtNumCons(c: Double): String = String.format(Locale.ITALY, "%.1f", c)

internal fun daysAgoText(day: LocalDate): String {
    val n = ChronoUnit.DAYS.between(day, LocalDate.now())
    return when {
        n <= 0L -> "oggi"
        n == 1L -> "ieri"
        else -> "$n giorni fa"
    }
}

internal fun String.toDec(): Double? = replace(',', '.').toDoubleOrNull()