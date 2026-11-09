package com.example.gpltrack.data.model

import java.time.LocalDate

data class Refuel(
    val id: Long,
    val epochDay: Long,
    val odometer: Int,
    val kmGpl: Int,
    val liters: Double,
    val cost: Double,
    val fuel: Fuel = Fuel.Benzina,
    val fullTank: Boolean = true
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
    val pricePerLiter: Double get() = cost / liters
}