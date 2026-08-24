package com.matelab.islas.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.model.WorldTheme
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.paletteFor

/**
 * Ilustracion de cada isla del archipielago.
 * Las cuatro comparten la base rocosa pero tienen un edificio propio, para
 * que se distingan de un vistazo en el mapa.
 */
@Composable
fun IslandArt(
    theme: WorldTheme,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    locked: Boolean = false
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val u = this.size.width / 100f
            drawIsland(u, theme, locked)
        }
    }
}

private fun DrawScope.drawIsland(u: Float, theme: WorldTheme, locked: Boolean) {
    val palette = paletteFor(theme)
    val gray = Color(0xFF7C949B)
    val primary = if (locked) gray else palette.primary
    val dark = if (locked) Color(0xFF5C737A) else palette.dark
    val soft = if (locked) Color(0xFFA7BAC0) else palette.soft
    val accent = if (locked) Color(0xFF8FA5AB) else palette.accent
    val sand = if (locked) Color(0xFFB9C6CA) else Sand

    // Roca base
    val rock = Path().apply {
        moveTo(12 * u, 78 * u)
        lineTo(24 * u, 60 * u)
        lineTo(74 * u, 60 * u)
        lineTo(88 * u, 78 * u)
        quadraticBezierTo(50 * u, 92 * u, 12 * u, 78 * u)
        close()
    }
    drawPath(rock, dark)

    // Playa
    val beach = Path().apply {
        moveTo(20 * u, 64 * u)
        quadraticBezierTo(50 * u, 54 * u, 80 * u, 64 * u)
        quadraticBezierTo(50 * u, 74 * u, 20 * u, 64 * u)
        close()
    }
    drawPath(beach, sand)

    when (theme) {
        WorldTheme.FORMAS -> drawLighthouse(u, primary, dark, accent, soft, locked)
        WorldTheme.MEDIDA -> drawHarbour(u, primary, dark, accent, soft)
        WorldTheme.FRACCION -> drawVolcano(u, primary, dark, accent, soft)
        WorldTheme.NUMEROS -> drawCave(u, primary, dark, accent, soft)
    }

    if (locked) {
        // Candado sobre la isla sumergida
        drawRoundRect(
            color = Deep.copy(alpha = 0.55f),
            topLeft = Offset(42 * u, 34 * u),
            size = Size(16 * u, 13 * u),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3 * u, 3 * u)
        )
        drawArc(
            color = Deep.copy(alpha = 0.55f),
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(45 * u, 27 * u),
            size = Size(10 * u, 12 * u),
            style = Stroke(width = 3f * u)
        )
    }
}

// ------------------------------------------------------------ Bahia de Formas

private fun DrawScope.drawLighthouse(
    u: Float,
    primary: Color,
    dark: Color,
    accent: Color,
    soft: Color,
    locked: Boolean
) {
    // Torre
    val tower = Path().apply {
        moveTo(42 * u, 60 * u)
        lineTo(45 * u, 20 * u)
        lineTo(55 * u, 20 * u)
        lineTo(58 * u, 60 * u)
        close()
    }
    drawPath(tower, Sand)
    // Franjas
    for (i in 0..2) {
        val y = 26f + i * 11f
        val band = Path().apply {
            moveTo((44.2f - i * 0.35f) * u, y * u)
            lineTo((55.8f + i * 0.35f) * u, y * u)
            lineTo((56.1f + i * 0.35f) * u, (y + 5) * u)
            lineTo((43.9f - i * 0.35f) * u, (y + 5) * u)
            close()
        }
        drawPath(band, primary)
    }
    // Cabina
    drawRect(dark, Offset(42 * u, 13 * u), Size(16 * u, 8 * u))
    drawPath(polygonPath(Offset(50 * u, 9 * u), 10 * u, 3, 0f), accent)
    if (!locked) {
        // Haz de luz
        val beam = Path().apply {
            moveTo(58 * u, 15 * u)
            lineTo(96 * u, 6 * u)
            lineTo(96 * u, 26 * u)
            close()
        }
        drawPath(beam, Sun.copy(alpha = 0.45f))
    }
    // Figuras flotantes
    drawPath(polygonPath(Offset(22 * u, 30 * u), 8 * u, 3, 10f), soft)
    drawPath(polygonPath(Offset(78 * u, 42 * u), 7 * u, 6, 0f), accent.copy(alpha = 0.85f))
    drawCircle(primary.copy(alpha = 0.75f), 5.5f * u, Offset(30 * u, 48 * u))
}

// --------------------------------------------------------------- Puerto Medida

private fun DrawScope.drawHarbour(
    u: Float,
    primary: Color,
    dark: Color,
    accent: Color,
    soft: Color
) {
    // Grua
    drawRect(dark, Offset(30 * u, 24 * u), Size(4 * u, 36 * u))
    drawRect(dark, Offset(30 * u, 24 * u), Size(38 * u, 4 * u))
    drawLine(dark, Offset(64 * u, 28 * u), Offset(64 * u, 40 * u), strokeWidth = 1.6f * u)
    drawRect(primary, Offset(58 * u, 40 * u), Size(12 * u, 10 * u))

    // Cajas
    drawRect(accent, Offset(38 * u, 48 * u), Size(12 * u, 12 * u))
    drawRect(soft, Offset(50 * u, 52 * u), Size(9 * u, 8 * u))
    drawLine(dark, Offset(38 * u, 54 * u), Offset(50 * u, 54 * u), strokeWidth = 1.2f * u)

    // Mastil-regla con marcas
    drawRect(Sand, Offset(76 * u, 20 * u), Size(7 * u, 40 * u))
    for (i in 0..7) {
        val y = (23f + i * 4.6f) * u
        val long = i % 2 == 0
        drawLine(
            dark,
            Offset(76 * u, y),
            Offset((if (long) 82f else 80f) * u, y),
            strokeWidth = 1.1f * u
        )
    }

    // Reloj del puerto
    drawCircle(Sand, 8 * u, Offset(20 * u, 34 * u))
    drawCircle(dark, 8 * u, Offset(20 * u, 34 * u), style = Stroke(width = 1.8f * u))
    drawLine(dark, Offset(20 * u, 34 * u), Offset(20 * u, 29 * u), strokeWidth = 1.6f * u)
    drawLine(dark, Offset(20 * u, 34 * u), Offset(24 * u, 35 * u), strokeWidth = 1.4f * u)
}

// -------------------------------------------------------------- Volcan Fraccion

private fun DrawScope.drawVolcano(
    u: Float,
    primary: Color,
    dark: Color,
    accent: Color,
    soft: Color
) {
    val cone = Path().apply {
        moveTo(24 * u, 62 * u)
        lineTo(42 * u, 18 * u)
        lineTo(58 * u, 18 * u)
        lineTo(76 * u, 62 * u)
        close()
    }
    drawPath(cone, dark)

    val slope = Path().apply {
        moveTo(50 * u, 18 * u)
        lineTo(76 * u, 62 * u)
        lineTo(50 * u, 62 * u)
        close()
    }
    drawPath(slope, primary.copy(alpha = 0.55f))

    // Crater partido en porciones (guino a las fracciones)
    drawOval(accent, Offset(40 * u, 13 * u), Size(20 * u, 9 * u))
    for (i in 0..3) {
        val a = -80f + i * 50f
        val end = onCircle(Offset(50 * u, 17.5f * u), 10 * u, a)
        drawLine(dark, Offset(50 * u, 17.5f * u), Offset(end.x, end.y * 0.45f + 9.6f * u), strokeWidth = 1.2f * u)
    }

    // Lava
    val lava = Path().apply {
        moveTo(47 * u, 18 * u)
        quadraticBezierTo(44 * u, 34 * u, 52 * u, 44 * u)
        quadraticBezierTo(58 * u, 54 * u, 54 * u, 62 * u)
        lineTo(44 * u, 62 * u)
        quadraticBezierTo(48 * u, 48 * u, 42 * u, 36 * u)
        quadraticBezierTo(40 * u, 26 * u, 47 * u, 18 * u)
        close()
    }
    drawPath(lava, Sun)

    // Humo
    drawCircle(soft.copy(alpha = 0.55f), 6 * u, Offset(58 * u, 8 * u))
    drawCircle(soft.copy(alpha = 0.40f), 4.5f * u, Offset(66 * u, 4 * u))
}

// ------------------------------------------------------------ Cueva de Numeros

private fun DrawScope.drawCave(
    u: Float,
    primary: Color,
    dark: Color,
    accent: Color,
    soft: Color
) {
    val hill = Path().apply {
        moveTo(20 * u, 62 * u)
        quadraticBezierTo(30 * u, 14 * u, 52 * u, 16 * u)
        quadraticBezierTo(74 * u, 18 * u, 80 * u, 62 * u)
        close()
    }
    drawPath(hill, dark)

    // Boca de la cueva
    val mouth = Path().apply {
        moveTo(38 * u, 62 * u)
        quadraticBezierTo(38 * u, 34 * u, 52 * u, 34 * u)
        quadraticBezierTo(66 * u, 34 * u, 66 * u, 62 * u)
        close()
    }
    drawPath(mouth, Color(0xFF0A2530))

    // Cristales
    drawCrystalShard(Offset(30 * u, 52 * u), 9 * u, primary)
    drawCrystalShard(Offset(72 * u, 48 * u), 12 * u, accent)
    drawCrystalShard(Offset(52 * u, 56 * u), 8 * u, soft)

    // Brillo interior
    drawCircle(accent.copy(alpha = 0.30f), 7 * u, Offset(52 * u, 50 * u))
}

private fun DrawScope.drawCrystalShard(base: Offset, height: Float, color: Color) {
    val w = height * 0.42f
    val path = Path().apply {
        moveTo(base.x, base.y)
        lineTo(base.x - w, base.y - height * 0.55f)
        lineTo(base.x - w * 0.25f, base.y - height)
        lineTo(base.x + w * 0.7f, base.y - height * 0.62f)
        close()
    }
    drawPath(path, color)
    drawPath(path, Deep.copy(alpha = 0.35f), style = Stroke(width = 1.2f))
}

/**
 * Version pequena y girada de la isla, util como icono de modulo.
 */
@Composable
fun IslandBadge(theme: WorldTheme, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val u = this.size.width / 100f
            rotate(-6f) { drawIsland(u, theme, locked = false) }
        }
    }
}
