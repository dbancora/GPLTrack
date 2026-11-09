package com.example.gpltrack.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.ui.theme.Night
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
private fun BlurLayer(
    hazeState: HazeState,
    blur: Dp,
    tintAlpha: Float,
    solidUntil: Float,
    fadeEnd: Float,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    val stops = remember(solidUntil, fadeEnd) {
        buildList {
            add(0f to Color.Black)
            for (i in 0..10) {
                val t = i / 10f
                val a = 1f - t * t * (3f - 2f * t)
                add((solidUntil + (fadeEnd - solidUntil) * t) to Color.Black.copy(alpha = a))
            }
            add(1f to Color.Transparent)
        }.toTypedArray()
    }

    Box(
        modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                alpha = progress()
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(colorStops = stops),
                    blendMode = BlendMode.DstIn
                )
            }
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Night,
                    tint = HazeTint(Night.copy(alpha = tintAlpha)),
                    blurRadius = blur,
                    noiseFactor = 0f
                )
            )
    )
}

@Composable
internal fun TopGlassBar(
    title: String,
    scroll: ScrollState,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    action: (@Composable () -> Unit)? = null
) {
    val progress by remember { derivedStateOf { (scroll.value / 40f).coerceIn(0f, 1f) } }

    Box(modifier.fillMaxWidth()) {
        BlurLayer(hazeState, blur = 2.dp, tintAlpha = 0.04f, solidUntil = 0.15f, fadeEnd = 1.00f,
            progress = { progress }, modifier = Modifier.matchParentSize())
        BlurLayer(hazeState, blur = 6.dp, tintAlpha = 0.09f, solidUntil = 0.30f, fadeEnd = 0.92f,
            progress = { progress }, modifier = Modifier.matchParentSize())
        BlurLayer(hazeState, blur = 18.dp, tintAlpha = 0.18f, solidUntil = 0.45f, fadeEnd = 0.80f,
            progress = { progress }, modifier = Modifier.matchParentSize())

        Column {
            Row(
                Modifier
                    .statusBarsPadding()
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                action?.invoke()
            }
            Spacer(Modifier.height(56.dp))
        }
    }
}