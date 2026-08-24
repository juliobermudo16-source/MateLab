package com.matelab.islas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = Deep,
    secondary = Mango,
    onSecondary = Color.White,
    secondaryContainer = MangoSoft,
    onSecondaryContainer = Color(0xFF5A2C05),
    tertiary = Violet,
    onTertiary = Color.White,
    tertiaryContainer = VioletSoft,
    onTertiaryContainer = Color(0xFF2C1E6B),
    background = Sand,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8F4F2),
    onSurfaceVariant = InkSoft,
    outline = Color(0xFFBBD6D3),
    error = Coral,
    onError = Color.White
)

private val DarkScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Color(0xFF00312C),
    primaryContainer = Color(0xFF0C5F58),
    onPrimaryContainer = TealSoft,
    secondary = Mango,
    onSecondary = Color(0xFF3A1A00),
    secondaryContainer = Color(0xFF7A3D0F),
    onSecondaryContainer = MangoSoft,
    tertiary = Violet,
    onTertiary = Color(0xFF1B1050),
    tertiaryContainer = Color(0xFF3E2C9E),
    onTertiaryContainer = VioletSoft,
    background = NightBg,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = NightInkSoft,
    outline = Color(0xFF23606F),
    error = Color(0xFFFF7B7E),
    onError = Color(0xFF3F0710)
)

val LocalMateColors = staticCompositionLocalOf { LightMateColors }

/** True cuando el nino ha desactivado las animaciones en los ajustes. */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** True cuando el nino ha activado el texto grande. */
val LocalBigText = staticCompositionLocalOf { false }

@Composable
fun MateLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reducedMotion: Boolean = false,
    bigText: Boolean = false,
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val mateColors = if (darkTheme) DarkMateColors else LightMateColors
    val typography = if (bigText) scaledTypography(1.18f) else MateTypography

    CompositionLocalProvider(
        LocalMateColors provides mateColors,
        LocalReducedMotion provides reducedMotion,
        LocalBigText provides bigText
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = typography,
            shapes = MateShapes,
            content = content
        )
    }
}

/** Atajo para leer la paleta extendida desde cualquier composable. */
object MateTheme {
    val colors: MateColors
        @Composable get() = LocalMateColors.current
}
