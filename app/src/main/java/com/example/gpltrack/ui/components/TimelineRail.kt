package com.example.gpltrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gpltrack.ui.theme.Night

@Composable
internal fun TimelineRail(
    dotColor: Color,
    topColor: Color,
    bottomColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    isLatestOfFuel: Boolean
) {
    Canvas(Modifier.width(22.dp).fillMaxHeight()) {
        val cx = size.width / 2
        val dotY = 24.dp.toPx()
        val top = if (isFirst) dotY else 0f
        val bottom = if (isLast) dotY else size.height
        val dotFrac = if (bottom - top > 0f) ((dotY - top) / (bottom - top)).coerceIn(0f, 1f) else 0f

        if (bottom > top) {
            drawLine(
                brush = Brush.verticalGradient(
                    0f to topColor,
                    dotFrac to dotColor,
                    1f to bottomColor,
                    startY = top,
                    endY = bottom
                ),
                start = Offset(cx, top),
                end = Offset(cx, bottom),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        if (isLatestOfFuel) {
            drawCircle(dotColor.copy(alpha = 0.30f), radius = 12.dp.toPx(), center = Offset(cx, dotY))
            drawCircle(dotColor, radius = 6.5.dp.toPx(), center = Offset(cx, dotY))
            drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5.dp.toPx(), center = Offset(cx, dotY))
        } else {
            drawCircle(Night, radius = 5.5.dp.toPx(), center = Offset(cx, dotY))
            drawCircle(dotColor, radius = 4.5.dp.toPx(), center = Offset(cx, dotY))
        }
    }
}