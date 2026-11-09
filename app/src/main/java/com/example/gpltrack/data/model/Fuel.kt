package com.example.gpltrack.data.model

import androidx.compose.ui.graphics.Color
import com.example.gpltrack.ui.theme.DusterOrange
import com.example.gpltrack.ui.theme.Teal

enum class Fuel(val label: String, val color: Color) {
    Benzina("Benzina", DusterOrange),
    GPL("GPL", Teal)
}