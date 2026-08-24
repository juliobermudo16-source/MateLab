package com.matelab.islas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Tipografia de MateLab.
 *
 * Se usa la familia del sistema (sin ficheros de fuente) pero con pesos y
 * tamanos elegidos para ninos de 8 a 12: titulares muy marcados y cuerpos
 * comodos de leer, sin llegar al tamano de preescolar.
 */
private val Sans = FontFamily.SansSerif

val MateTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Black,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Black,
        fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp, lineHeight = 31.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = 27.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 17.sp, lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 15.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.Bold,
        fontSize = 12.sp, lineHeight = 15.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Sans, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    )
)

/**
 * Version agrandada de la tipografia para el ajuste de "texto grande".
 * Se escala todo por igual para que las proporciones no se rompan.
 */
fun scaledTypography(factor: Float): Typography {
    fun TextStyle.scale(): TextStyle = copy(
        fontSize = fontSize * factor,
        lineHeight = lineHeight * factor
    )
    return Typography(
        displayLarge = MateTypography.displayLarge.scale(),
        displayMedium = MateTypography.displayMedium.scale(),
        displaySmall = MateTypography.displaySmall.scale(),
        headlineLarge = MateTypography.headlineLarge.scale(),
        headlineMedium = MateTypography.headlineMedium.scale(),
        headlineSmall = MateTypography.headlineSmall.scale(),
        titleLarge = MateTypography.titleLarge.scale(),
        titleMedium = MateTypography.titleMedium.scale(),
        titleSmall = MateTypography.titleSmall.scale(),
        bodyLarge = MateTypography.bodyLarge.scale(),
        bodyMedium = MateTypography.bodyMedium.scale(),
        bodySmall = MateTypography.bodySmall.scale(),
        labelLarge = MateTypography.labelLarge.scale(),
        labelMedium = MateTypography.labelMedium.scale(),
        labelSmall = MateTypography.labelSmall.scale()
    )
}
