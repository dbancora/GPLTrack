package com.example.gpltrack.ui.screens

import android.Manifest
import android.R.style.Theme
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.data.model.Fuel
import com.example.gpltrack.data.model.Profile
import com.example.gpltrack.data.model.Refuel
import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.data.viewmodel.RefuelViewModel
import com.example.gpltrack.data.viewmodel.WashViewModel
import com.example.gpltrack.ui.components.GlassButton
import com.example.gpltrack.ui.components.GlassCard
import com.example.gpltrack.ui.components.PriceHistoryChart
import com.example.gpltrack.ui.components.ScreenScaffold
import com.example.gpltrack.ui.components.Stat
import com.example.gpltrack.ui.components.glass
import com.example.gpltrack.ui.theme.StatusOk
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.ui.theme.Teal
import com.example.gpltrack.ui.theme.WashBlue
import com.example.gpltrack.util.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
internal fun HomeScreen(vm: RefuelViewModel, wvm: WashViewModel, pvm: ProfileViewModel, onGoToRefuels: () -> Unit, onGoToProfile: () -> Unit) = ScreenScaffold("Home") {
    val list = vm.refuels
    val totals = totalsByFuel(list)
    val consumptions = consumptionMap(list)
    val profile = pvm.profile

    //Permesso per inviare notifiche:
    var hasAskedPermission by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> /* Puoi fare qualcosa se vuole */ }

    LaunchedEffect(Unit) {
        if (!hasAskedPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            hasAskedPermission = true
        }
    }

    //Aggiungo l'informazione sulla prossima scadenza

    val next = nextDeadline(profile)
    if (next != null){
        val (deadlineDaysText, deadlineDaysColor)  = deadlineStatus(next.epochDay, next.feminine)

        if (deadlineDaysColor != StatusOk) {
            NextDeadlineCard(profile = profile, onGoToProfile = onGoToProfile)
            Spacer(Modifier.height(16.dp))
        }
    }


    val currentOdometer = list.firstOrNull()?.odometer ?: 0

    GlassCard {
        Column {
            Text("Odometro auto", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))

            // Mostriamo l'odometro totale dell'auto in grande
            Text(
                fmtKm(currentOdometer),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                Fuel.entries.forEach { fuel ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(fuel.color, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        // Qui mostriamo i km totali fatti con ciascun carburante
                        Stat("Totale ${fuel.label}", fmtKm(totals[fuel] ?: 0))
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))



    val intervalKms = list
        .sortedBy { it.odometer }
        .zipWithNext { prev, current -> current.id to (current.odometer - prev.odometer) }
        .toMap()

    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Fuel.entries.forEach { fuel ->
            val last = list.firstOrNull { it.fuel == fuel }
            FuelSummaryCard(
                fuel = fuel,
                last = last,
                intervalKm = last?.let { intervalKms[it.id] ?: 0 } ?: 0,
                consumption = last?.let { consumptions[it.id] },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }

    if (list.isEmpty()) {
        Spacer(Modifier.height(16.dp))
        GlassButton("Aggiungi il primo rifornimento", accent = DusterOrange, onClick = onGoToRefuels)
    }
    Spacer(Modifier.height(16.dp))

    GlassCard {
        Column {
            // Cambiamo il titolo per riflettere che ora è una spesa totale
            Text("Spese mensili", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            // Aggiungiamo il parametro washes
            PriceHistoryChart(
                refuels = list,
                washes = wvm.washes,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Legenda aggiornata con i Lavaggi
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(DusterOrange, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("Benzina", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(Teal, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("GPL", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(WashBlue, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text("Lavaggi", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}


@Composable
private fun NextDeadlineCard(profile: Profile, onGoToProfile: () -> Unit) {
    val next = nextDeadline(profile)

    GlassCard(modifier = Modifier.clickable(onClick = onGoToProfile)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val accent = next?.let { deadlineStatus(it.epochDay, it.feminine).second }
                    ?: Color.White.copy(alpha = 0.5f)

                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    if (next == null) {
                        Text(
                            "Nessuna scadenza impostata",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            next.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,               // ← una sola riga
                            softWrap = false,           // ← non andare a capo
                            modifier = Modifier.basicMarquee()  // ← fai scorrere
                        )
                    }
                }

                if (next != null) {
                    val (statusText, statusColor) = deadlineStatus(next.epochDay, next.feminine)
                    val chip = RoundedCornerShape(14.dp)
                    Box(
                        Modifier
                            .clip(chip)
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), chip)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            statusText,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,               // ← una sola riga
                            softWrap = false,           // ← non andare a capo
                            modifier = Modifier.basicMarquee()  // ← fai scorrere
                        )
                    }
                }
            }

            if (next != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    fmtDate(LocalDate.ofEpochDay(next.epochDay)),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 54.dp) // allineato col titolo, dopo l'icona
                )
            }
        }
    }
}

@Composable
private fun FuelSummaryCard(
    fuel: Fuel,
    last: Refuel?,
    intervalKm: Int,         // ← AGGIUNTO
    consumption: Double?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Column(modifier.glass(shape).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(fuel.color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(fuel.label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text("Ultimo rifornimento", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))

        if (last == null) {
            Text("Nessun rifornimento", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        } else {
            Text(
                consumption?.let { fmtNumCons(it) } ?: "—",
                color = if (consumption != null) fuel.color else Color.White.copy(alpha = 0.5f),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                when {
                    consumption != null -> "L/100 km"
                    !last.fullTank -> "Pieno parziale"
                    else -> "Primo pieno completo"
                },
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))

            // Calcoliamo i km specifici per questo carburante
            val kmForThisFuel = if (fuel == Fuel.GPL) {
                last.kmGpl
            } else {
                (intervalKm - last.kmGpl).coerceAtLeast(0)
            }

            Stat("Km percorsi", fmtKm(kmForThisFuel))  // ← MODIFICATO
            Spacer(Modifier.height(12.dp))
            Stat("Prezzo al litro", fmtPrice(last.pricePerLiter))
        }
    }
}

@Composable
fun NotificationPermissionRequester() {
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    // Controlla se il permesso è già concesso
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            hasPermission = true // Su versioni precedenti non serve
        }
    }

    if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Button(onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
            Text("Abilita notifiche per le scadenze")
        }
    }
}