package com.example.gpltrack.util

import com.example.gpltrack.data.model.Fuel
import com.example.gpltrack.data.model.Refuel

fun consumptionMap(list: List<Refuel>): Map<Long, Double> {
    val result = mutableMapOf<Long, Double>()
    val ordered = list.sortedWith(compareBy<Refuel>({ it.odometer }, { it.id }))

    if (ordered.isEmpty()) return result

    var litersGpl = 0.0
    var kmGpl = 0
    var hasRefGpl = false

    var litersBenzina = 0.0
    var kmBenzina = 0
    var hasRefBenzina = false

    var prevOdometer = ordered.first().odometer

    ordered.forEach { r ->
        val kmTotaliIntervallo = r.odometer - prevOdometer
        val kmBenzinaIntervallo = kmTotaliIntervallo - r.kmGpl

        kmGpl += r.kmGpl
        kmBenzina += kmBenzinaIntervallo

        if (r.fuel == Fuel.GPL) {
            litersGpl += r.liters
            if (r.fullTank) {
                if (hasRefGpl && kmGpl > 0) {
                    result[r.id] = (litersGpl / kmGpl) * 100
                }
                hasRefGpl = true
                litersGpl = 0.0
                kmGpl = 0
            }
        } else {
            litersBenzina += r.liters
            if (r.fullTank) {
                if (hasRefBenzina && kmBenzina > 0) {
                    result[r.id] = (litersBenzina / kmBenzina) * 100
                }
                hasRefBenzina = true
                litersBenzina = 0.0
                kmBenzina = 0
            }
        }

        prevOdometer = r.odometer
    }
    return result
}

fun totalsByFuel(list: List<Refuel>): Map<Fuel, Int> {
    var gpl = 0
    var benzina = 0
    val ordered = list.sortedBy { it.odometer }

    ordered.forEachIndexed { index, r ->
        if (index > 0) {
            val prev = ordered[index - 1]
            val kmTotali = r.odometer - prev.odometer
            gpl += r.kmGpl
            benzina += (kmTotali - r.kmGpl)
        }
    }
    return mapOf(Fuel.GPL to gpl, Fuel.Benzina to benzina)
}

// Per la timeline, i km progressivi ora si basano sull'odometro
data class CumulativeProgress(val gpl: Int, val benzina: Int)

fun cumulativeKm(list: List<Refuel>): Map<Long, CumulativeProgress> {
    val result = mutableMapOf<Long, CumulativeProgress>()
    val ordered = list.sortedWith(compareBy<Refuel>({ it.odometer }, { it.id }))
    if (ordered.isEmpty()) return result

    var runningGpl = 0
    var runningBenzina = 0
    var prevOdometer = ordered.first().odometer

    ordered.forEachIndexed { index, r ->
        // Se è il primo rifornimento in assoluto (index == 0),
        // i km totali e i km GPL sono 0 (è solo il punto di partenza)
        val kmTotali = if (index == 0) 0 else r.odometer - prevOdometer
        val actualKmGpl = if (index == 0) 0 else r.kmGpl
        val kmBenzinaIntervallo = kmTotali - actualKmGpl

        runningGpl += actualKmGpl
        runningBenzina += kmBenzinaIntervallo

        result[r.id] = CumulativeProgress(runningGpl, runningBenzina)
        prevOdometer = r.odometer
    }
    return result
}