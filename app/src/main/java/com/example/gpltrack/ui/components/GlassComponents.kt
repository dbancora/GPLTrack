package com.example.gpltrack.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.navigation.Section
import com.example.gpltrack.ui.theme.Night

internal fun Modifier.glass(shape: Shape = RoundedCornerShape(28.dp)): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.07f))
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.30f)
            )
        ),
        shape = shape
    )

@Composable
internal fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().glass().padding(20.dp)) { content() }
}

@Composable
internal fun GlassIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .glass(CircleShape)
            .background(Color(0xFFFF7A1F).copy(alpha = 0.28f))   // DusterOrange
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Color.White)
    }
}

@Composable
fun GlassSnackbar(data: SnackbarData) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 1. Mettiamo prima lo sfondo scuro (come nei dialoghi)
            .background(Color(0xFF141A2E).copy(alpha = 0.92f), shape)
            // 2. Poi applichiamo l'effetto vetro
            .glass(shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.visuals.message,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
internal fun GlassButton(
    text: String,
    enabled: Boolean = true,
    accent: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .glass(shape)
            .background(accent.copy(alpha = if (enabled && accent != Color.Transparent) 0.55f else 0.2f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.4f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun GlassNavBar(selected: Section, onSelect: (Section) -> Unit, modifier: Modifier = Modifier) {
    val pill = RoundedCornerShape(36.dp)
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .background(Night.copy(alpha = 0.80f), pill)
            .glass(pill)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Section.entries.forEach { section ->
            val isSelected = section == selected
            val bg by animateColorAsState(
                if (isSelected) Color.White.copy(alpha = 0.22f) else Color.Transparent,
                label = "tabBg"
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(section) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    section.icon,
                    contentDescription = section.label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                )
                Text(
                    section.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}