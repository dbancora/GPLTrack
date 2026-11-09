package com.example.gpltrack.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Section(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Profilo("Il mio profilo", Icons.Filled.Person),
    Rifornimenti("Rifornimenti", Icons.Filled.LocalGasStation),
    Impostazioni("Impostazioni", Icons.Filled.Settings)
}