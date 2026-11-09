package com.example.gpltrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.ui.theme.Night
import com.example.gpltrack.ui.theme.Teal
import com.example.gpltrack.ui.theme.Violet

@Composable
internal fun AuroraBackground(modifier: Modifier = Modifier) {
    val shift = 0.5f
    Canvas(modifier.fillMaxSize().background(Night)) {
        val w = size.width
        val h = size.height
        fun blob(color: Color, cx: Float, cy: Float, r: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r
                ),
                radius = r,
                center = Offset(cx, cy)
            )
        }
        blob(DusterOrange.copy(alpha = 0.60f), w * (0.15f + 0.5f * shift), h * 0.18f, w * 0.85f)
        blob(Violet.copy(alpha = 0.45f), w * (0.95f - 0.5f * shift), h * 0.55f, w * 0.90f)
        blob(Teal.copy(alpha = 0.40f), w * (0.20f + 0.3f * shift), h * 0.92f, w * 0.80f)
    }
}