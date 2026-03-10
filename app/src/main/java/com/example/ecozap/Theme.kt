package com.example.ecozap
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Night900,
    secondary = NeonCyan,
    background = Night900,
    surface = CardDark,
    onBackground = Color.White,
    onSurface = Color.White,
    error = DangerRed
)

@Composable
fun EcoZapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
