package com.example.gpltrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.data.model.Fuel
import com.example.gpltrack.data.model.Refuel
import com.example.gpltrack.data.model.Wash
import com.example.gpltrack.ui.theme.WashBlue
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PriceHistoryChart(
    refuels: List<Refuel>,
    washes: List<Wash>,
    modifier: Modifier = Modifier
) {
    if (refuels.isEmpty() && washes.isEmpty()) return

    // 1. Uniamo tutti i mesi in cui c'è stata almeno una spesa
    val months = remember(refuels, washes) {
        val refuelMonths = refuels.map { YearMonth.from(it.date) }
        val washMonths = washes.map { YearMonth.from(it.date) }
        (refuelMonths + washMonths).distinct().sorted()
    }

    if (months.isEmpty()) return

    // 2. Animazione progressiva
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(refuels, washes) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(durationMillis = 1500, easing = LinearEasing))
    }

    val textMeasurer = rememberTextMeasurer()
    val monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        // Calcoliamo il valore massimo di spesa mensile per scalare l'asse Y
        val monthlyTotals = months.map { month ->
            val benzina = refuels.filter { YearMonth.from(it.date) == month && it.fuel == Fuel.Benzina }.sumOf { it.cost }
            val gpl = refuels.filter { YearMonth.from(it.date) == month && it.fuel == Fuel.GPL }.sumOf { it.cost }
            val wash = washes.filter { YearMonth.from(it.date) == month }.sumOf { it.cost }
            maxOf(benzina, gpl, wash) // Prendiamo il massimo tra le tre categorie per quel mese
        }
        val maxCost = (monthlyTotals.maxOrNull() ?: 0.0).coerceAtLeast(10.0) // Minimo 10€ per non avere grafici piatti

        val yPadding = maxCost * 0.15
        val yMax = maxCost + yPadding

        // Margini del grafico (Ho aumentato rightMargin a 24.dp per far stare il pallino!)
        val leftMargin = 50.dp.toPx()
        val bottomMargin = 30.dp.toPx()
        val rightMargin = 24.dp.toPx()
        val topMargin = 20.dp.toPx()

        val chartWidth = size.width - leftMargin - rightMargin
        val chartHeight = size.height - bottomMargin - topMargin

        fun xForMonth(index: Int): Float {
            if (months.size <= 1) return leftMargin + chartWidth / 2f
            return leftMargin + (index.toFloat() / (months.size - 1)) * chartWidth
        }

        fun yForCost(cost: Double): Float {
            val ratio = (cost / yMax).toFloat()
            return topMargin + chartHeight - (ratio * chartHeight)
        }

        // --- DISEGNO ASSI ED ETICHETTE ---
        drawLine(Color.White.copy(alpha = 0.2f), Offset(leftMargin, topMargin), Offset(leftMargin, topMargin + chartHeight), strokeWidth = 2f)
        drawLine(Color.White.copy(alpha = 0.2f), Offset(leftMargin, topMargin + chartHeight), Offset(leftMargin + chartWidth, topMargin + chartHeight), strokeWidth = 2f)

        // Etichette Mesi (Asse X)
        months.forEachIndexed { index, month ->
            val x = xForMonth(index)
            val label = month.format(monthFormatter).replaceFirstChar { it.uppercase() }
            val textLayout = textMeasurer.measure(label, style = TextStyle(color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp))
            // Allineamento centrato
            drawText(textLayout, topLeft = Offset(x - textLayout.size.width / 2, topMargin + chartHeight + 8.dp.toPx()))
        }

        // Etichette Costi (Asse Y)
        val steps = 3
        for (i in 0..steps) {
            val cost = yMax * (i.toFloat() / steps)
            val y = yForCost(cost)
            val label = String.format(Locale.ITALY, "€ %.0f", cost) // Arrotondiamo a euro interi per pulizia
            val textLayout = textMeasurer.measure(label, style = TextStyle(color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp))
            drawText(textLayout, topLeft = Offset(leftMargin - textLayout.size.width - 8.dp.toPx(), y - textLayout.size.height / 2))

            drawLine(Color.White.copy(alpha = 0.05f), Offset(leftMargin, y), Offset(leftMargin + chartWidth, y), strokeWidth = 1f)
        }

        // --- DISEGNO LINEE ANIMATE ---
        // Aggiungiamo un piccolo margine in più al clipRect per non tagliare il pallino finale
        clipRect(right = leftMargin + chartWidth * animProgress.value + 10.dp.toPx()) {

            // Funzione di supporto per disegnare una specifica categoria
            fun drawLineForCategory(
                color: Color,
                getMonthlyCost: (YearMonth) -> Double
            ) {
                val path = Path()
                var isFirstPoint = true
                val points = mutableListOf<Offset>()

                months.forEachIndexed { index, month ->
                    val cost = getMonthlyCost(month)

                    // Disegniamo il punto solo se c'è stata una spesa in quel mese
                    if (cost > 0.0) {
                        val x = xForMonth(index)
                        val y = yForCost(cost)

                        if (isFirstPoint) {
                            path.moveTo(x, y)
                            isFirstPoint = false
                        } else {
                            path.lineTo(x, y)
                        }
                        points.add(Offset(x, y))
                    }
                }

                if (points.isNotEmpty()) {
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    points.forEach { point ->
                        drawCircle(color = color, radius = 4.dp.toPx(), center = point)
                        drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 1.5.dp.toPx(), center = point)
                    }
                }
            }

            // Disegniamo le tre linee
            drawLineForCategory(Fuel.Benzina.color) { month ->
                refuels.filter { YearMonth.from(it.date) == month && it.fuel == Fuel.Benzina }.sumOf { it.cost }
            }
            drawLineForCategory(Fuel.GPL.color) { month ->
                refuels.filter { YearMonth.from(it.date) == month && it.fuel == Fuel.GPL }.sumOf { it.cost }
            }
            drawLineForCategory(WashBlue) { month ->
                washes.filter { YearMonth.from(it.date) == month }.sumOf { it.cost }
            }
        }
    }
}