package com.example.ecozap.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen, onPrimary = Night900,
    secondary = NeonCyan, onSecondary = Night900,
    tertiary = NeonBlue, onTertiary = Night900,
    primaryContainer = CardDark, onPrimaryContainer = NeonGreen,
    secondaryContainer = CardDark, onSecondaryContainer = NeonCyan,
    background = Night900, onBackground = Color.White,
    surface = Night900, onSurface = Color.White,
    surfaceVariant = CardDark, onSurfaceVariant = TextDim,
    surfaceContainerLowest = Night900, surfaceContainerLow = Night800,
    surfaceContainer = CardDark, surfaceContainerHigh = CardDarker,
    surfaceContainerHighest = Night800, outline = TextDim,
    error = DangerRed, onError = Night900
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EcoZapTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}