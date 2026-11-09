package com.example.gpltrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.data.viewmodel.RefuelViewModel
import com.example.gpltrack.data.viewmodel.WashViewModel
import com.example.gpltrack.navigation.Section
import com.example.gpltrack.ui.components.AuroraBackground
import com.example.gpltrack.ui.components.GlassNavBar
import com.example.gpltrack.ui.screens.HomeScreen
import com.example.gpltrack.ui.screens.ProfileScreen
import com.example.gpltrack.ui.screens.RefuelScreen
import com.example.gpltrack.ui.screens.SettingsScreen
import com.example.gpltrack.ui.theme.LocalHazeState
import com.example.gpltrack.ui.theme.Night
import com.example.gpltrack.util.createNotificationChannel
import com.example.gpltrack.util.scheduleDailyDeadlineCheck
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    private val vm: RefuelViewModel by viewModels()
    private val profileVm: ProfileViewModel by viewModels()
    private val washVm: WashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        // 1. Crea il canale di notifica (obbligatorio da Android 8+)
        createNotificationChannel(this)

        // 2. Pianifica il controllo giornaliero (se non è già pianificato)
        scheduleDailyDeadlineCheck(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DusterApp(vm, profileVm, washVm)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun DusterApp(vm: RefuelViewModel, pvm: ProfileViewModel, wvm: WashViewModel) {
    var selected by rememberSaveable { mutableStateOf(Section.Home) }
    val hazeState = rememberHazeState()

    CompositionLocalProvider(LocalHazeState provides hazeState, LocalOverscrollFactory provides null) {
        Box(Modifier.fillMaxSize()) {
            AuroraBackground(Modifier.hazeSource(hazeState, zIndex = 0f))

            Crossfade(targetState = selected, label = "section") { section ->
                when (section) {
                    Section.Home -> HomeScreen(vm = vm, wvm = wvm, pvm = pvm, onGoToRefuels = { selected = Section.Rifornimenti }, onGoToProfile = { selected = Section.Profilo })
                    Section.Profilo -> ProfileScreen(pvm)
                    Section.Rifornimenti -> RefuelScreen(vm, wvm)
                    Section.Impostazioni -> SettingsScreen(vm,wvm,pvm)
                }
            }

            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(150.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Night.copy(alpha = 0.92f))))
            )

            GlassNavBar(
                selected = selected,
                onSelect = { selected = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}