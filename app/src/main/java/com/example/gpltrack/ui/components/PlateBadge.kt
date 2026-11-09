package com.example.gpltrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PlateBadge(plate: String) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .clip(shape)
            .background(Color.White)
            .border(1.5.dp, Color(0xFF1B1B1B), shape)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            Modifier.fillMaxHeight().width(26.dp).background(Color(0xFF0B3D91)),
            contentAlignment = Alignment.Center
        ) {
            Text("I", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            plate.ifBlank { "TARGA" },
            color = if (plate.isBlank()) Color(0xFFB0B6C2) else Color(0xFF111111),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}