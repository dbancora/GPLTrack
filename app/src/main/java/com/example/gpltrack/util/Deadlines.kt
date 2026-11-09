package com.example.gpltrack.util

import androidx.compose.ui.graphics.Color
import com.example.gpltrack.data.model.Profile
import com.example.gpltrack.ui.theme.StatusDanger
import com.example.gpltrack.ui.theme.StatusOk
import com.example.gpltrack.ui.theme.StatusWarn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Una scadenza "concreta" del profilo: titolo, data, genere grammaticale per i messaggi. */
internal data class DeadlineInfo(
    val title: String,
    val epochDay: Long,
    val feminine: Boolean
)

/** Testo e colore per una scadenza: verde se lontana, giallo entro 30 giorni, rosso entro 7 o scaduta. */
internal fun deadlineStatus(epochDay: Long?, feminine: Boolean): Pair<String, Color> {
    if (epochDay == null) return "Non impostata" to Color.White.copy(alpha = 0.5f)
    val days = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(epochDay))
    val expired = if (feminine) "Scaduta" else "Scaduto"
    return when {
        days < -1L -> "$expired da ${-days} giorni" to StatusDanger
        days == -1L -> "$expired ieri" to StatusDanger
        days == 0L -> "Scade oggi" to StatusDanger
        days == 1L -> "Scade domani" to StatusDanger
        days <= 7L -> "Scade tra $days giorni" to StatusDanger
        days <= 30L -> "Scade tra $days giorni" to StatusWarn
        else -> "Scade tra $days giorni" to StatusOk
    }
}

/**
 * Restituisce la scadenza più imminente tra tutte quelle impostate nel profilo.
 * Politica: se una è già scaduta, ha priorità su quelle future — è quella da gestire per prima.
 * Restituisce null se il profilo non ha nessuna scadenza impostata.
 */
internal fun nextDeadline(profile: Profile): DeadlineInfo? {
    val candidates = buildList {
        profile.bolloExpiry?.let { add(DeadlineInfo("Bollo auto", it, feminine = false)) }
        profile.insuranceExpiry?.let { add(DeadlineInfo("Assicurazione", it, feminine = true)) }
        profile.licencePlateExpire?.let { add(DeadlineInfo("Patente", it, feminine = true)) }
    }
    return candidates.minByOrNull { it.epochDay }
}

// DBDB: Not used function
// Non la uso questa funzione ma potrebbe essermi utile per poi l'aggiunta delle notifiche
internal fun daysBetween(epochDay: Long?): Long {
    if (epochDay == null) return 365L;

    return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(epochDay))
}