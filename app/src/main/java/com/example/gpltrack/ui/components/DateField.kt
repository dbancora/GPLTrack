package com.example.gpltrack.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.util.fmtDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateField(label: String, epochDay: Long?, onChange: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (epochDay != null) "$label: ${fmtDate(LocalDate.ofEpochDay(epochDay))}"
            else "$label: non impostata",
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        if (epochDay != null) {
            Text(
                "Rimuovi",
                color = DusterOrange,
                fontSize = 13.sp,
                modifier = Modifier.clickable { onChange(null) }.padding(8.dp)
            )
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (epochDay ?: LocalDate.now().toEpochDay()) * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(it / 86_400_000L) }
                    showPicker = false
                }) { Text("Ok") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Annulla") } }
        ) {
            DatePicker(state = state)
        }
    }
}