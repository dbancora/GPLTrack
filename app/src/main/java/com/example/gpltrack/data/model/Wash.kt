package com.example.gpltrack.data.model

import java.time.LocalDate

enum class WashType(val label: String) {
    Manuale("Manuale"),
    Automatico("Automatico")
}

data class Wash(
    val id: Long,
    val epochDay: Long,
    val type: WashType,
    val interior: Boolean,
    val cost: Double
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(epochDay)
}