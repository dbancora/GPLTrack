package com.example.gpltrack.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.example.gpltrack.ui.theme.DusterOrange

@Composable
fun DusterSilhouette(modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(205f / 88f)) {
        scale(scale = size.width / 205f, pivot = Offset.Zero) {
            val clad = Color(0xFF2A2F3A)
            val silver = Color(0xFF9AA3B2)
            val well = Color(0xFF080B14)
            val glass = Color(0xFF0A0F1E).copy(alpha = 0.80f)

            fun poly(vararg p: Pair<Float, Float>) = Path().apply {
                moveTo(p[0].first, p[0].second)
                for (i in 1 until p.size) lineTo(p[i].first, p[i].second)
                close()
            }

            drawOval(Color.Black.copy(alpha = 0.35f), topLeft = Offset(12f, 76f), size = Size(184f, 9f))

            val body = Path().apply {
                moveTo(8f, 63f)
                lineTo(7f, 40f); lineTo(10f, 33f); lineTo(12f, 17f); lineTo(17f, 11f)
                lineTo(118f, 11f); lineTo(140f, 33f)
                lineTo(184f, 36f); lineTo(194f, 41f); lineTo(196f, 50f); lineTo(196f, 63f)
                lineTo(176f, 63f)
                arcTo(Rect(140f, 45f, 176f, 81f), 0f, -180f, false)
                lineTo(63f, 63f)
                arcTo(Rect(27f, 45f, 63f, 81f), 0f, -180f, false)
                close()
            }
            drawPath(
                body,
                brush = Brush.verticalGradient(
                    0f to Color(0xFFFF9A52),
                    0.5f to DusterOrange,
                    1f to Color(0xFFC9560F),
                    startY = 8f,
                    endY = 62f
                )
            )

            drawRect(clad, topLeft = Offset(63f, 55f), size = Size(77f, 8f))
            drawRect(clad, topLeft = Offset(176f, 54f), size = Size(20f, 9f))
            drawRect(clad, topLeft = Offset(8f, 54f), size = Size(19f, 9f))
            drawPath(body, Color.White.copy(alpha = 0.18f), style = Stroke(width = 0.8f))

            drawPath(poly(17f to 34f, 19f to 17f, 62f to 17f, 62f to 34f), glass)
            drawPath(poly(68f to 34f, 68f to 17f, 120f to 17f, 137f to 34f), glass)

            drawLine(silver, Offset(24f, 8f), Offset(112f, 8f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(silver, Offset(34f, 8f), Offset(34f, 11f), strokeWidth = 2f)
            drawLine(silver, Offset(102f, 8f), Offset(102f, 11f), strokeWidth = 2f)

            drawLine(Color.White.copy(alpha = 0.22f), Offset(14f, 38f), Offset(188f, 38f), strokeWidth = 1f)
            drawLine(Color.Black.copy(alpha = 0.25f), Offset(65f, 34f), Offset(65f, 55f), strokeWidth = 0.8f)
            drawLine(Color.Black.copy(alpha = 0.25f), Offset(124f, 34f), Offset(124f, 54f), strokeWidth = 0.8f)
            drawLine(Color.Black.copy(alpha = 0.5f), Offset(72f, 40f), Offset(79f, 40f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            drawLine(Color.Black.copy(alpha = 0.5f), Offset(30f, 40f), Offset(37f, 40f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            drawLine(clad, Offset(128f, 32f), Offset(134f, 32f), strokeWidth = 3.5f, cap = StrokeCap.Round)

            drawLine(Color.White, Offset(184f, 38f), Offset(194f, 41f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawRect(well, topLeft = Offset(190f, 44f), size = Size(6f, 6f))
            drawLine(Color(0xFFD32F2F), Offset(11f, 22f), Offset(11f, 32f), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(silver, Offset(180f, 58f), Offset(194f, 58f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            drawLine(silver, Offset(10f, 58f), Offset(25f, 58f), strokeWidth = 2.5f, cap = StrokeCap.Round)

            for (cx in listOf(45f, 158f)) {
                drawArc(well, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(cx - 18f, 45f), size = Size(36f, 36f))
                drawArc(clad, startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(cx - 19f, 44f), size = Size(38f, 38f),
                    style = Stroke(width = 3.5f))
                drawCircle(Color(0xFF11141C), radius = 15f, center = Offset(cx, 63f))
                drawCircle(Color(0xFFB9C0CC), radius = 9f, center = Offset(cx, 63f))
                drawCircle(Color(0xFF3A4150), radius = 3.5f, center = Offset(cx, 63f))
            }
        }
    }
}