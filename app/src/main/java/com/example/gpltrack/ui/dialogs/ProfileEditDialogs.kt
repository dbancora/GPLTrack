package com.example.gpltrack.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gpltrack.data.model.Profile
import com.example.gpltrack.ui.components.DateField
import com.example.gpltrack.ui.components.GlassField
import com.example.gpltrack.ui.theme.DusterOrange

@Composable
internal fun VehicleEditDialog(profile: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var model by remember { mutableStateOf(profile.model) }
    var version by remember { mutableStateOf(profile.version) }
    var year by remember { mutableStateOf(profile.year) }
    var plate by remember { mutableStateOf(profile.plate) }

    EditDialog(
        title = "Dati vettura",
        onDismiss = onDismiss,
        onSave = {
            onSave(
                profile.copy(
                    model = model.trim(),
                    version = version.trim(),
                    year = year.trim(),
                    plate = plate.trim()
                )
            )
        }
    ) {
        GlassField(model, { model = it }, "Modello", KeyboardType.Text, DusterOrange)
        GlassField(version, { version = it }, "Allestimento / motorizzazione", KeyboardType.Text, DusterOrange)
        GlassField(year, { year = it.filter(Char::isDigit).take(4) }, "Anno", KeyboardType.Number, DusterOrange)
        GlassField(
            plate,
            { plate = it.uppercase().filter(Char::isLetterOrDigit).take(8) },
            "Targa",
            KeyboardType.Text,
            DusterOrange
        )
    }
}

@Composable
internal fun OwnerEditDialog(profile: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(profile.ownerName) }
    //var phone by remember { mutableStateOf(profile.ownerPhone) }
    //var email by remember { mutableStateOf(profile.ownerEmail) }

    EditDialog(
        title = "Proprietario",
        onDismiss = onDismiss,
        onSave = {
            onSave(profile.copy(
                ownerName = name.trim(),
                //ownerPhone = phone.trim(),
                //ownerEmail = email.trim()
            ))
        }
    ) {
        GlassField(name, { name = it }, "Nome e cognome", KeyboardType.Text, DusterOrange)
        //GlassField(phone, { phone = it }, "Telefono", KeyboardType.Phone, DusterOrange)
        //GlassField(email, { email = it }, "Email", KeyboardType.Email, DusterOrange)
    }
}

@Composable
internal fun DeadlinesEditDialog(profile: Profile, onDismiss: () -> Unit, onSave: (Profile) -> Unit) {
    var bollo by remember { mutableStateOf(profile.bolloExpiry) }
    var insurance by remember { mutableStateOf(profile.insuranceExpiry) }
    var company by remember { mutableStateOf(profile.insuranceCompany) }
    var policy by remember { mutableStateOf(profile.policyNumber) }
    //Aggiungo per la patente: --db
    var licence by remember { mutableStateOf(profile.licencePlateNumber) }
    var licenceExp by remember { mutableStateOf(profile.licencePlateExpire) }

    EditDialog(
        title = "Scadenze",
        onDismiss = onDismiss,
        onSave = {
            onSave(profile.copy(
                bolloExpiry = bollo,
                insuranceExpiry = insurance,
                licencePlateExpire = licenceExp,
                insuranceCompany = company.trim(),
                policyNumber = policy.trim(),
                licencePlateNumber = licence.trim()
            ))
        }
    ) {
        DateField("Scadenza bollo", bollo) { bollo = it }

        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
        Spacer(Modifier.height(6.dp))

        DateField("Scadenza assicurazione", insurance) { insurance = it }
        GlassField(company, { company = it }, "Compagnia assicurativa", KeyboardType.Text, DusterOrange)
        GlassField(policy, { policy = it }, "Numero polizza", KeyboardType.Text, DusterOrange)

        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
        Spacer(Modifier.height(6.dp))

        DateField("Scadenza patente", licenceExp) { licenceExp = it }
        GlassField(licence, { licence = it }, "Numero patente", KeyboardType.Text, DusterOrange)
    }
}