package com.example.gpltrack.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.data.backup.BackupManager
import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.data.viewmodel.RefuelViewModel
import com.example.gpltrack.data.viewmodel.WashViewModel
import com.example.gpltrack.ui.components.GlassButton
import com.example.gpltrack.ui.components.GlassCard
import com.example.gpltrack.ui.components.ScreenScaffold

@Composable
internal fun SettingsScreen(vm: RefuelViewModel, wvm: WashViewModel, pvm: ProfileViewModel
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                out.write(BackupManager.export(vm, wvm, pvm).toByteArray())
            }
            message = "Backup completo esportato."
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                val json = context.contentResolver.openInputStream(it)?.use { input ->
                    input.readBytes().decodeToString()
                } ?: error("Impossibile leggere il file.")
                BackupManager.import(json, vm, wvm, pvm)
            }.onSuccess { r ->
                message = buildString {
                    append("Importati ${r.refuels} rifornimenti, ${r.washes} lavaggi")
                    if (r.profileImported) append(" e profilo")
                    append(".")
                }
            }.onFailure { e ->
                message = "Errore durante l'importazione: ${e.message}"
            }
        }
    }

    ScreenScaffold("Impostazioni") {
        GlassCard {
            Column {
                Text("Backup dati", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esporta tutti i rifornimenti in un file JSON, oppure importa un backup precedente.",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassButton("Esporta") {
                        exportLauncher.launch("duster_tracker_backup.json")
                    }
                    GlassButton("Importa") {
                        importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }
                }
                message?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
        }

        //Spacer(Modifier.height(16.dp))
    }
}