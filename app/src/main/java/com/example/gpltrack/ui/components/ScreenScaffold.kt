package com.example.gpltrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gpltrack.ui.theme.LocalHazeState
import dev.chrisbanes.haze.hazeSource

@Composable
internal fun ScreenScaffold(title: String, content: @Composable () -> Unit) {
    val scroll = rememberScrollState()
    val hazeState = LocalHazeState.current

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .hazeSource(hazeState, zIndex = 1f)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.statusBarsPadding().height(84.dp))
            content()
            Spacer(Modifier.height(130.dp))
        }
        TopGlassBar(title, scroll, hazeState, Modifier.align(Alignment.TopCenter))
    }
}