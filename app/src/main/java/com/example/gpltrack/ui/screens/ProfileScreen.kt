package com.example.gpltrack.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gpltrack.data.viewmodel.ProfileViewModel
import com.example.gpltrack.ui.components.*
import com.example.gpltrack.ui.dialogs.DeadlinesEditDialog
import com.example.gpltrack.ui.dialogs.OwnerEditDialog
import com.example.gpltrack.ui.dialogs.VehicleEditDialog
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.ui.theme.StatusDanger
import com.example.gpltrack.util.deadlineStatus
import com.example.gpltrack.util.fmtDate
import java.time.LocalDate
import com.example.gpltrack.R

private enum class ProfileSection { Vehicle, Owner, Deadlines }

@Composable
internal fun ProfileScreen(pvm: ProfileViewModel) {
    val profile = pvm.profile
    val photo = pvm.photo
    var editing by remember { mutableStateOf<ProfileSection?>(null) }
    var photoError by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoError = !pvm.setPhoto(uri)
    }

    ScreenScaffold("Il mio profilo") {
        val heroShape = RoundedCornerShape(28.dp)
        Column(Modifier.fillMaxWidth().glass(heroShape)) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                if (photo != null) {
                    Image(photo, contentDescription = "Foto della vettura",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.radialGradient(listOf(DusterOrange.copy(alpha = 0.35f), Color.Transparent))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.duster_default),
                            contentDescription = "Dacia Duster",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        )
                    }
                }

                Box(Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                    GlassIconButton(Icons.Filled.PhotoCamera, "Cambia foto") {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }

                if (photo != null) {
                    Box(
                        Modifier.align(Alignment.BottomEnd).padding(12.dp)
                            .glass(RoundedCornerShape(16.dp))
                            .clickable { pvm.clearPhoto() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Immagine predefinita", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            profile.model.ifBlank { "La mia vettura" },
                            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                        val subtitle = listOf(profile.version, profile.year)
                            .filter { it.isNotBlank() }.joinToString(" • ")
                        if (subtitle.isNotEmpty()) {
                            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                    SmallEditButton { editing = ProfileSection.Vehicle }
                }
                Spacer(Modifier.height(14.dp))
                PlateBadge(profile.plate)
                if (photoError) {
                    Spacer(Modifier.height(10.dp))
                    Text("Non sono riuscito a usare questa immagine.",
                        color = StatusDanger, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Column {
                SectionHeader("Scadenze") { editing = ProfileSection.Deadlines }
                Spacer(Modifier.height(12.dp))
                DeadlineRow("Bollo auto", profile.bolloExpiry, feminine = false, subtitle = null)
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
                Spacer(Modifier.height(12.dp))
                DeadlineRow(
                    "Assicurazione", profile.insuranceExpiry, feminine = true,
                    subtitle = listOf(profile.insuranceCompany, profile.policyNumber)
                        .filter { it.isNotBlank() }.joinToString(" • ").ifBlank { null }
                )
                //Aggiungo anche la patente
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
                Spacer(Modifier.height(12.dp))
                DeadlineRow(
                    "Patente", profile.licencePlateExpire, feminine = true,
                    subtitle = listOf(profile.licencePlateNumber)
                        .filter { it.isNotBlank() }.joinToString(" • ").ifBlank { null }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        GlassCard {
            Column {
                SectionHeader("Proprietario") { editing = ProfileSection.Owner }
                Spacer(Modifier.height(12.dp))
                Stat(value = profile.ownerName.ifBlank { "—" })
                Spacer(Modifier.height(12.dp))
                //Stat("Telefono", profile.ownerPhone.ifBlank { "—" })
                //Spacer(Modifier.height(12.dp))
                //Stat("Email", profile.ownerEmail.ifBlank { "—" })
            }
        }
    }

    when (editing) {
        ProfileSection.Vehicle -> VehicleEditDialog(
            profile = profile,
            onDismiss = { editing = null },
            onSave = { newProfile -> pvm.update { newProfile }; editing = null }
        )
        ProfileSection.Owner -> OwnerEditDialog(
            profile = profile,
            onDismiss = { editing = null },
            onSave = { newProfile -> pvm.update { newProfile }; editing = null }
        )
        ProfileSection.Deadlines -> DeadlinesEditDialog(
            profile = profile,
            onDismiss = { editing = null },
            onSave = { newProfile -> pvm.update { newProfile }; editing = null }
        )
        null -> Unit
    }
}

@Composable
private fun SmallEditButton(onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).glass(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Edit, contentDescription = "Modifica",
            tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SectionHeader(title: String, onEdit: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f))
        SmallEditButton(onEdit)
    }
}

@Composable
private fun DeadlineRow(title: String, epochDay: Long?, feminine: Boolean, subtitle: String?) {
    val (statusText, statusColor) = deadlineStatus(epochDay, feminine)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (epochDay != null) {
                Text(fmtDate(LocalDate.ofEpochDay(epochDay)),
                    color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        val chip = RoundedCornerShape(14.dp)
        Box(
            Modifier.clip(chip)
                .background(statusColor.copy(alpha = 0.18f))
                .border(1.dp, statusColor.copy(alpha = 0.5f), chip)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}