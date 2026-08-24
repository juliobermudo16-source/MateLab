package com.matelab.islas.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.matelab.islas.domain.model.WorldTheme

/**
 * Paleta de MateLab: mar turquesa, arena calida y acentos vivos.
 * Cada isla tiene su propia gama para que el archipielago no se vea plano.
 */

val Deep = Color(0xFF0B3B4A)
val DeepSoft = Color(0xFF11576B)
val Teal = Color(0xFF12B3A6)
val TealDark = Color(0xFF0E8F88)
val TealSoft = Color(0xFF8FE3DC)
val Mango = Color(0xFFFF8A3D)
val MangoDark = Color(0xFFE0691C)
val MangoSoft = Color(0xFFFFD3AE)
val Violet = Color(0xFF7C5CFF)
val VioletDark = Color(0xFF5B3FD6)
val VioletSoft = Color(0xFFCFC4FF)
val Sun = Color(0xFFFFC846)
val SunDark = Color(0xFFE0A616)
val Coral = Color(0xFFF2585B)
val CoralDark = Color(0xFFC93B41)
val CoralSoft = Color(0xFFFFC2C4)
val Lime = Color(0xFF7ED957)
val Aqua = Color(0xFF59E0C5)

val Sand = Color(0xFFFFF6E9)
val SandDeep = Color(0xFFF6E4CB)
val Paper = Color(0xFFFFFFFF)
val Ink = Color(0xFF10313D)
val InkSoft = Color(0xFF54737E)

val NightBg = Color(0xFF07202A)
val NightSurface = Color(0xFF0E3441)
val NightSurfaceHigh = Color(0xFF15485A)
val NightInk = Color(0xFFEAF6F5)
val NightInkSoft = Color(0xFF9CBFC7)

/** Colores que no cubre Material 3 pero que la app usa por todas partes. */
@Immutable
data class MateColors(
    val canvasTop: Color,
    val canvasBottom: Color,
    val card: Color,
    val cardAlt: Color,
    val ink: Color,
    val inkSoft: Color,
    val outline: Color,
    val locked: Color,
    val star: Color,
    val success: Color,
    val warning: Color,
    val sea: Color,
    val seaDeep: Color,
    val sand: Color
)

val LightMateColors = MateColors(
    canvasTop = Color(0xFFCDF2EE),
    canvasBottom = Sand,
    card = Paper,
    cardAlt = Color(0xFFF3FBFA),
    ink = Ink,
    inkSoft = InkSoft,
    outline = Color(0xFFD3E6E4),
    locked = Color(0xFFB6C7CC),
    star = Sun,
    success = Color(0xFF23A26D),
    warning = Coral,
    sea = Teal,
    seaDeep = Deep,
    sand = SandDeep
)

val DarkMateColors = MateColors(
    canvasTop = Color(0xFF0A2B38),
    canvasBottom = NightBg,
    card = NightSurface,
    cardAlt = NightSurfaceHigh,
    ink = NightInk,
    inkSoft = NightInkSoft,
    outline = Color(0xFF1E5567),
    locked = Color(0xFF335D6B),
    star = Sun,
    success = Color(0xFF43C88E),
    warning = Color(0xFFFF7B7E),
    sea = TealDark,
    seaDeep = Color(0xFF041821),
    sand = Color(0xFF23414A)
)

/** Gama propia de cada isla: se usa en mapas, tarjetas y mini-juegos. */
@Immutable
data class WorldPalette(
    val primary: Color,
    val dark: Color,
    val soft: Color,
    val accent: Color
)

fun paletteFor(theme: WorldTheme): WorldPalette = when (theme) {
    WorldTheme.FORMAS -> WorldPalette(Teal, TealDark, TealSoft, Sun)
    WorldTheme.MEDIDA -> WorldPalette(Mango, MangoDark, MangoSoft, Deep)
    WorldTheme.FRACCION -> WorldPalette(Coral, CoralDark, CoralSoft, Sun)
    WorldTheme.NUMEROS -> WorldPalette(Violet, VioletDark, VioletSoft, Aqua)
}

/** Colores de las piezas de los patrones y de las figuras del clasificador. */
val ShapePalette = listOf(Teal, Mango, Violet, Sun, Coral, Lime)
