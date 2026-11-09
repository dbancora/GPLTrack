package com.example.gpltrack.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gpltrack.data.model.Fuel
import com.example.gpltrack.data.model.Refuel
import com.example.gpltrack.ui.components.FuelChip
import com.example.gpltrack.ui.components.GlassButton
import com.example.gpltrack.ui.components.GlassField
import com.example.gpltrack.ui.components.glass
import com.example.gpltrack.util.fmtDate
import com.example.gpltrack.util.fmtKm
import com.example.gpltrack.util.toDec
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddRefuelDialog(
    lastRefuel: Refuel?,
    onDismiss: () -> Unit,
    onSave: (Refuel) -> Unit
) {
    var fuel by remember { mutableStateOf(Fuel.GPL) }
    var date by remember { mutableStateOf(LocalDate.now()) }

    var odometer by remember { mutableStateOf("") }
    var kmGpl by remember { mutableStateOf("") }

    var liters by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var fullTank by remember { mutableStateOf(true) }
    var showPicker by remember { mutableStateOf(false) }

    val odometerValue = odometer.toIntOrNull()
    val kmGplValue = kmGpl.toIntOrNull()
    val litersValue = liters.toDec()
    val costValue = cost.toDec()

    val lastOdometer = lastRefuel?.odometer ?: 0

    // <-- MODIFICATO QUI: Variabile per capire se è il primo rifornimento
    val isFirstRefuel = lastRefuel == null

    val kmTotaliIntervallo = if (odometerValue != null && lastRefuel != null) odometerValue - lastOdometer else null
    val kmBenzinaIntervallo = if (kmTotaliIntervallo != null && kmGplValue != null) kmTotaliIntervallo - kmGplValue else null

    val valid = odometerValue != null && odometerValue > lastOdometer &&
            // Se è il primo rifornimento, non pretendiamo i km GPL
            (isFirstRefuel || (kmGplValue != null && kmGplValue >= 0)) &&
            (kmTotaliIntervallo == null || kmGplValue == null || kmGplValue <= kmTotaliIntervallo) &&
            litersValue != null && litersValue > 0 &&
            costValue != null && costValue > 0

    val shape = RoundedCornerShape(32.dp)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A2E).copy(alpha = 0.92f), shape)
                .glass(shape)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Nuovo rifornimento", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Fuel.entries.forEach { f ->
                    FuelChip(f, selected = f == fuel, modifier = Modifier.weight(1f)) { fuel = f }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { showPicker = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text("Data: ${fmtDate(date)}", color = Color.White)
            }

            // Campo Odometro (sempre attivo)
            GlassField(
                value = odometer,
                onValueChange = { odometer = it.filter(Char::isDigit) },
                label = "Odometro totale auto (km)",
                keyboardType = KeyboardType.Number,
                accent = fuel.color
            )

            // <-- MODIFICATO QUI: Campo Km GPL disabilitato se è il primo rifornimento
            GlassField(
                value = if (isFirstRefuel) "" else kmGpl,
                onValueChange = { if (!isFirstRefuel) kmGpl = it.filter(Char::isDigit) },
                label = if (isFirstRefuel) "Km a GPL (non disponibili nel primo rifornimento)"
                else "Km percorsi a GPL dall'ultimo rifornimento",
                keyboardType = KeyboardType.Number,
                accent = fuel.color.copy(alpha = if (isFirstRefuel) 0.5f else 1f)
            )

            // <-- MODIFICATO QUI: Messaggio di aiuto se è il primo rifornimento
            if (isFirstRefuel) {
                Text(
                    "Questo è il primo rifornimento: verrà usato solo come punto di partenza per l'odometro. I km percorsi inizieranno a essere calcolati dal prossimo rifornimento.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            } else if (kmTotaliIntervallo != null && kmBenzinaIntervallo != null) {
                // Anteprima normale per i rifornimenti successivi
                val isInvalid = kmBenzinaIntervallo < 0
                Text(
                    text = if (isInvalid)
                        "Errore: i Km a GPL superano i km totali percorsi ($kmTotaliIntervallo km)."
                    else
                        "Percorsi: $kmTotaliIntervallo km (di cui $kmGplValue a GPL e $kmBenzinaIntervallo a benzina)",
                    color = if (isInvalid) Color.Red else fuel.color,
                    fontSize = 13.sp
                )
            }

            GlassField(liters, { liters = it }, "Litri", KeyboardType.Decimal, fuel.color)
            GlassField(cost, { cost = it }, "Costo totale (€)", KeyboardType.Decimal, fuel.color)

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Serbatoio pieno", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (fullTank) "Il consumo (L/100 km) verrà calcolato"
                        else "Rifornimento parziale: il consumo si calcolerà al prossimo pieno completo",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = fullTank,
                    onCheckedChange = { fullTank = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = fuel.color,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.35f)
                    )
                )
            }

            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Annulla", color = Color.White.copy(alpha = 0.7f)) }
                Spacer(Modifier.width(8.dp))
                GlassButton("Salva", enabled = valid, accent = fuel.color) {
                    // <-- MODIFICATO QUI: Al salvataggio, se è il primo rifornimento, kmGpl diventa 0
                    onSave(
                        Refuel(
                            id = System.currentTimeMillis(),
                            epochDay = date.toEpochDay(),
                            odometer = odometerValue!!,
                            kmGpl = if (isFirstRefuel) 0 else kmGplValue!!,
                            liters = litersValue!!,
                            cost = costValue!!,
                            fuel = fuel,
                            fullTank = fullTank
                        )
                    )
                }
            }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { date = LocalDate.ofEpochDay(it / 86_400_000L) }
                    showPicker = false
                }) { Text("Ok") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Annulla") } }
        ) {
            DatePicker(state = state)
        }
    }
}