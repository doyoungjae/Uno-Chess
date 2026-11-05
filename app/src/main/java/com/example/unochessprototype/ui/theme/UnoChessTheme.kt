package com.example.unochessprototype.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

object UnoChessColors {
    val Background = Color(0xFF08140D)
    val Surface = Color(0xFF102219)
    val SurfaceRaised = Color(0xFF173126)
    val SurfaceSoft = Color(0xFF1D392B)
    val Lime = Color(0xFFA8E85A)
    val LimeDark = Color(0xFF6AAA35)
    val Text = Color(0xFFF4F7F2)
    val Muted = Color(0xFFAAB8AE)
    val Border = Color(0xFF2D4A38)
    val Warning = Color(0xFFF2C94C)
    val Danger = Color(0xFFE56D6D)
    val Blue = Color(0xFF6DA8FF)
}

@Immutable
data class BoardPalette(
    val light: Color,
    val dark: Color,
    val border: Color,
    val label: String,
)

fun boardPalette(theme: Int): BoardPalette = when (theme) {
    1 -> BoardPalette(
        light = Color(0xFFF0D9B5),
        dark = Color(0xFF8B5A3C),
        border = Color(0xFF4A2C21),
        label = "Walnut",
    )
    2 -> BoardPalette(
        light = Color(0xFFCAD2D7),
        dark = Color(0xFF52636F),
        border = Color(0xFF28343C),
        label = "Slate",
    )
    else -> BoardPalette(
        light = Color(0xFFC7D9A7),
        dark = Color(0xFF41634A),
        border = Color(0xFF26382D),
        label = "Forest",
    )
}

private val AppColorScheme = darkColorScheme(
    primary = UnoChessColors.Lime,
    onPrimary = Color(0xFF0B160D),
    primaryContainer = Color(0xFF28461D),
    onPrimaryContainer = Color(0xFFDFFFC2),
    secondary = UnoChessColors.Blue,
    background = UnoChessColors.Background,
    onBackground = UnoChessColors.Text,
    surface = UnoChessColors.Surface,
    onSurface = UnoChessColors.Text,
    surfaceVariant = UnoChessColors.SurfaceRaised,
    onSurfaceVariant = UnoChessColors.Muted,
    outline = UnoChessColors.Border,
    error = UnoChessColors.Danger,
)

@Composable
fun UnoChessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content,
    )
}
