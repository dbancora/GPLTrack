package com.example.gpltrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.data.model.Fuel            // ← AGGIUNTO
import com.example.gpltrack.data.model.Refuel
import com.example.gpltrack.data.model.Wash
import com.example.gpltrack.data.viewmodel.RefuelViewModel
import com.example.gpltrack.data.viewmodel.WashViewModel
import com.example.gpltrack.ui.components.*             // ← include glass, StatRow, TimelineRail, TopGlassBar, GlassIconButton
import com.example.gpltrack.ui.dialogs.AddChooserDialog
import com.example.gpltrack.ui.dialogs.AddRefuelDialog
import com.example.gpltrack.ui.dialogs.AddWashDialog
import com.example.gpltrack.ui.theme.*                  // ← Night, DusterOrange, WashBlue, LocalHazeState
import com.example.gpltrack.util.*                      // ← tutti i fmt*, daysAgoText, cumulativeKm, consumptionMap, totalsByFuel
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
internal fun RefuelScreen(vm: RefuelViewModel, wvm: WashViewModel) {
    var showChooser by remember { mutableStateOf(false) }
    var showAddRefuel by remember { mutableStateOf(false) }
    var showAddWash by remember { mutableStateOf(false) }
    var refuelToDelete by remember { mutableStateOf<Refuel?>(null) }
    var washToDelete by remember { mutableStateOf<Wash?>(null) }
    val list = vm.refuels
    val scroll = rememberScrollState()
    val hazeState = LocalHazeState.current


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize()
                .hazeSource(hazeState, zIndex = 1f)
                .verticalScroll(scroll)
                .padding(horizontal = 14.dp)
        ) {
            Spacer(Modifier.statusBarsPadding().height(84.dp))
            UnifiedTimeline(list, wvm.washes, { refuelToDelete = it }, { washToDelete = it })
            Spacer(Modifier.height(130.dp))
        }
        TopGlassBar("Rifornimenti", scroll, hazeState,
            modifier = Modifier.align(Alignment.TopCenter), horizontalPadding = 14.dp) {
            GlassIconButton(Icons.Filled.Add, "Aggiungi") { showChooser = true }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp) // Alzo per evitare che vada sotto la navbar
                .padding(horizontal = 20.dp),
            snackbar = { data ->
                GlassSnackbar(data)
            }
        )
    }

    if (showChooser) {
        AddChooserDialog(
            onDismiss = { showChooser = false },
            onRefuel = { showChooser = false; showAddRefuel = true },
            onWash = { showChooser = false; showAddWash = true }
        )
    }

    if (showAddRefuel) {
        AddRefuelDialog(
            lastRefuel = list.firstOrNull(), // <-- Passiamo l'ultimo rifornimento (o null se è il primo)
            onDismiss = { showAddRefuel = false },
            onSave = {
                vm.add(it)
                showAddRefuel = false

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Rifornimento salvato! Ricordati di resettare il parziale dell'auto.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    if (showAddWash) {
        AddWashDialog(
            onDismiss = { showAddWash = false },
            onSave = { wvm.add(it); showAddWash = false }
        )
    }

    refuelToDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { refuelToDelete = null },
            containerColor = Color(0xFF141A2E),
            title = { Text("Eliminare il rifornimento?", color = Color.White) },
            text = { Text("${r.fuel.label} • ${fmtDate(r.date)} • ${fmtLiters(r.liters)}",
                color = Color.White.copy(alpha = 0.75f)) },
            confirmButton = { TextButton(onClick = { vm.delete(r.id); refuelToDelete = null }) {
                Text("Elimina", color = DusterOrange) } },
            dismissButton = { TextButton(onClick = { refuelToDelete = null }) { Text("Annulla") } }
        )
    }

    washToDelete?.let { w ->
        AlertDialog(
            onDismissRequest = { washToDelete = null },
            containerColor = Color(0xFF141A2E),
            title = { Text("Eliminare il lavaggio?", color = Color.White) },
            text = { Text("${fmtDate(w.date)} • ${w.type.label} • ${fmtEuro(w.cost)}",
                color = Color.White.copy(alpha = 0.75f)) },
            confirmButton = { TextButton(onClick = { wvm.delete(w.id); washToDelete = null }) {
                Text("Elimina", color = DusterOrange) } },
            dismissButton = { TextButton(onClick = { washToDelete = null }) { Text("Annulla") } }
        )
    }
}

// ---------- Elementi privati della timeline ----------

private sealed class TimelineItem(val epochDay: Long, val id: Long) {
    class RefuelItem(val refuel: Refuel) : TimelineItem(refuel.epochDay, refuel.id)
    class WashItem(val wash: Wash) : TimelineItem(wash.epochDay, wash.id)
}

@Composable
private fun RefuelCard(
    refuel: Refuel,
    isLatest: Boolean,
    cumKm: CumulativeProgress,
    intervalKm: Int,     // ← Assicurati di passare questo parametro dalla Timeline!
    consumption: Double?,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    // Calcolo dei km specifici per questa card
    // Se intervalKm è 0, significa che è il primo rifornimento in assoluto:
    // non ci sono km percorsi da mostrare in questo intervallo.
    val kmForThisFuel = if (intervalKm == 0) 0 else if (refuel.fuel == Fuel.GPL) {
        refuel.kmGpl
    } else {
        (intervalKm - refuel.kmGpl).coerceAtLeast(0)
    }

    val progForThisFuel = if (refuel.fuel == Fuel.GPL) cumKm.gpl else cumKm.benzina

    Column(
        Modifier
            .fillMaxWidth()
            .glass(shape)
            .background(if (isLatest) refuel.fuel.color.copy(alpha = 0.14f) else Color.Transparent)
            .padding(start = 12.dp, top = 8.dp, end = 4.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                fmtDate(refuel.date),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Elimina",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            if (isLatest) "Ultimo • ${daysAgoText(refuel.date)}" else daysAgoText(refuel.date),
            color = if (isLatest) refuel.fuel.color else Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(end = 8.dp)
        )
        if (!refuel.fullTank) {
            Text(
                "Pieno parziale",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            StatRow("Km fatti", fmtNumKm(kmForThisFuel))
            StatRow("Km progr.", fmtNumKm(if (refuel.fuel == Fuel.GPL) cumKm.gpl else cumKm.benzina))
            StatRow("Litri", fmtNumL(refuel.liters))
            StatRow("Costo", fmtEuro(refuel.cost))
            StatRow("€/L", fmtNumPrice(refuel.pricePerLiter))
            StatRow("L/100 km", consumption?.let { fmtNumCons(it) } ?: "—")
        }
    }
}

@Composable
private fun WashCard(wash: Wash, isLatest: Boolean, onDelete: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Night.copy(alpha = 0.92f), shape)
            .glass(shape)
            .background(WashBlue.copy(alpha = if (isLatest) 0.18f else 0.10f))
            .padding(start = 12.dp, top = 10.dp, end = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(WashBlue.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocalCarWash, contentDescription = null, tint = WashBlue)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(fmtDate(wash.date), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                "Lavaggio ${wash.type.label.lowercase(Locale.ITALIAN)} • " +
                        if (wash.interior) "interni inclusi" else "solo esterno",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                if (isLatest) "Ultimo • ${daysAgoText(wash.date)}" else daysAgoText(wash.date),
                color = if (isLatest) WashBlue else Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = if (isLatest) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        Text(fmtEuro(wash.cost), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Elimina",
                tint = Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UnifiedTimeline(
    refuels: List<Refuel>,
    washes: List<Wash>,
    onDeleteRefuel: (Refuel) -> Unit,
    onDeleteWash: (Wash) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Fuel.entries.forEachIndexed { i, fuel ->
            Row(
                Modifier.weight(1f),
                horizontalArrangement = if (i == 0) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).background(fuel.color, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(fuel.label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            if (i == 0) Spacer(Modifier.width(22.dp))
        }
    }

    val items = buildList<TimelineItem> {
        refuels.forEach { add(TimelineItem.RefuelItem(it)) }
        washes.forEach { add(TimelineItem.WashItem(it)) }
    }.sortedWith(compareByDescending<TimelineItem> { it.epochDay }.thenByDescending { it.id })

    if (items.isEmpty()) {
        Text("Nessun rifornimento o lavaggio", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        return
    }

    val faded = Color.White.copy(alpha = 0.18f)
    fun colorAt(i: Int): Color {
        val base = when (val item = items[i.coerceIn(0, items.lastIndex)]) {
            is TimelineItem.RefuelItem -> item.refuel.fuel.color
            is TimelineItem.WashItem -> WashBlue
        }
        return lerp(base, faded, (i / 4f).coerceIn(0f, 1f))
    }

    val latestIds = Fuel.entries.mapNotNull { f -> refuels.firstOrNull { it.fuel == f }?.id }.toSet()
    val latestWashId = washes.firstOrNull()?.id

    // Ora 'cum' contiene un oggetto CumulativeProgress per ogni rifornimento
    val cum = cumulativeKm(refuels)
    val cons = consumptionMap(refuels)

    val intervalKms = refuels
        .sortedBy { it.odometer }
        .zipWithNext { prev, current -> current.id to (current.odometer - prev.odometer) }
        .toMap()

    items.forEachIndexed { index, item ->
        when (item) {
            is TimelineItem.RefuelItem -> {
                val refuel = item.refuel

                // Valore di default se il rifornimento non è nella mappa (es. il primo in assoluto)
                val cumulativeProgress = cum[refuel.id] ?: CumulativeProgress(0, 0)

                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(Modifier.weight(1f).padding(bottom = 12.dp, end = 4.dp)) {
                        if (refuel.fuel == Fuel.Benzina) {
                            RefuelCard(
                                refuel = refuel,
                                isLatest = refuel.id in latestIds,
                                cumKm = cumulativeProgress, // <-- Passiamo l'oggetto
                                intervalKm = intervalKms[refuel.id] ?: 0,
                                consumption = cons[refuel.id],
                                onDelete = { onDeleteRefuel(refuel) }
                            )
                        }
                    }
                    TimelineRail(
                        dotColor = colorAt(index),
                        topColor = lerp(colorAt(index - 1), colorAt(index), 0.5f),
                        bottomColor = lerp(colorAt(index), colorAt(index + 1), 0.5f),
                        isFirst = index == 0,
                        isLast = index == items.lastIndex,
                        isLatestOfFuel = refuel.id in latestIds
                    )
                    Box(Modifier.weight(1f).padding(bottom = 12.dp, start = 4.dp)) {
                        if (refuel.fuel == Fuel.GPL) {
                            RefuelCard(
                                refuel = refuel,
                                isLatest = refuel.id in latestIds,
                                cumKm = cumulativeProgress, // <-- Passiamo l'oggetto
                                intervalKm = intervalKms[refuel.id] ?: 0,
                                consumption = cons[refuel.id],
                                onDelete = { onDeleteRefuel(refuel) }
                            )
                        }
                    }
                }
            }

            is TimelineItem.WashItem -> {
                val wash = item.wash
                Box(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(Modifier.align(Alignment.Center).fillMaxHeight()) {
                        TimelineRail(
                            dotColor = colorAt(index),
                            topColor = lerp(colorAt(index - 1), colorAt(index), 0.5f),
                            bottomColor = lerp(colorAt(index), colorAt(index + 1), 0.5f),
                            isFirst = index == 0,
                            isLast = index == items.lastIndex,
                            isLatestOfFuel = false
                        )
                    }
                    Box(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        WashCard(wash, isLatest = wash.id == latestWashId) { onDeleteWash(wash) }
                    }
                }
            }
        }
    }
}