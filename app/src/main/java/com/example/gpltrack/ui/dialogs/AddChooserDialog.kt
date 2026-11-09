package com.example.gpltrack.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gpltrack.ui.components.glass
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.ui.theme.WashBlue

@Composable
internal fun AddChooserDialog(
    onDismiss: () -> Unit,
    onRefuel: () -> Unit,
    onWash: () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF141A2E).copy(alpha = 0.92f), shape)
                .glass(shape)
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cosa vuoi aggiungere?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            ChooserOption(Icons.Filled.LocalGasStation, "Rifornimento", DusterOrange, onRefuel)
            ChooserOption(Icons.Filled.LocalCarWash, "Lavaggio", WashBlue, onWash)
        }
    }
}

@Composable
private fun ChooserOption(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .glass(shape)
            .background(color.copy(alpha = 0.22f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(14.dp))
        Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}