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
import com.example.gpltrack.data.model.Wash
import com.example.gpltrack.data.model.WashType
import com.example.gpltrack.ui.components.ChoiceChip
import com.example.gpltrack.ui.components.GlassButton
import com.example.gpltrack.ui.components.GlassField
import com.example.gpltrack.ui.components.glass
import com.example.gpltrack.ui.theme.WashBlue
import com.example.gpltrack.util.fmtDate
import com.example.gpltrack.util.toDec
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddWashDialog(onDismiss: () -> Unit, onSave: (Wash) -> Unit) {
    var type by remember { mutableStateOf(WashType.Manuale) }
    var interior by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var cost by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    val costValue = cost.toDec()
    val valid = costValue != null && costValue >= 0
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
            Text("Nuovo lavaggio", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WashType.entries.forEach { t ->
                    ChoiceChip(t.label, selected = t == type, color = WashBlue, modifier = Modifier.weight(1f)) {
                        type = t
                    }
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

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Lavaggio interno", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (interior) "Interni inclusi" else "Solo esterno",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = interior,
                    onCheckedChange = { interior = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WashBlue,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.35f)
                    )
                )
            }

            GlassField(cost, { cost = it }, "Costo del lavaggio (€)", KeyboardType.Decimal, WashBlue)

            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("Annulla", color = Color.White.copy(alpha = 0.7f)) }
                Spacer(Modifier.width(8.dp))
                GlassButton("Salva", enabled = valid, accent = WashBlue) {
                    onSave(
                        Wash(
                            id = System.currentTimeMillis(),
                            epochDay = date.toEpochDay(),
                            type = type,
                            interior = interior,
                            cost = costValue!!
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