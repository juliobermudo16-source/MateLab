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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.ui.theme.Aqua
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Lime
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet

private val BadgeHues = listOf(Teal, Mango, Violet, Sun, Coral, Lime, Aqua)

/**
 * Insignia ilustrada. La semilla decide la forma del escudo, el color y el
 * emblema interior (regla, compas, estrella, fraccion, reloj, cristal...).
 */
@Composable
fun BadgeArt(
    artSeed: Int,
    unlocked: Boolean,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            drawBadge(this.size.width, artSeed, unlocked)
        }
    }
}

private fun DrawScope.drawBadge(side: Float, artSeed: Int, unlocked: Boolean) {
    val center = Offset(side / 2f, side / 2f)
    val hue = if (unlocked) BadgeHues[artSeed % BadgeHues.size] else Color(0xFFA9BCC1)
    val ring = if (unlocked) Sun else Color(0xFFC8D5D8)
    val shieldKind = artSeed % 3

    if (unlocked) drawCircle(hue.copy(alpha = 0.22f), side * 0.48f, center)

    // Cuerpo del escudo
    val body = when (shieldKind) {
        0 -> polygonPath(center, side * 0.36f, 6, 0f)
        1 -> polygonPath(center, side * 0.36f, 8, 22.5f)
        else -> Path().apply {
            val r = side * 0.36f
            moveTo(center.x - r, center.y - r * 0.85f)
            lineTo(center.x + r, center.y - r * 0.85f)
            lineTo(center.x + r, center.y + r * 0.2f)
            quadraticBezierTo(center.x, center.y + r * 1.25f, center.x - r, center.y + r * 0.2f)
            close()
        }
    }
    drawPath(body, hue)
    drawPath(body, ring, style = Stroke(width = side * 0.055f))

    // Emblema interior segun la semilla
    val emblem = artSeed % 7
    val ink = if (unlocked) Sand else Color(0xFFEFF4F5)
    when (emblem) {
        0 -> drawPath(starPath(center, side * 0.19f, side * 0.08f), ink)
        1 -> { // Regla
            drawRect(ink, Offset(center.x - side * 0.20f, center.y - side * 0.07f), Size(side * 0.40f, side * 0.14f))
            for (i in 1..3) {
                val x = center.x - side * 0.20f + side * 0.10f * i
                drawLine(hue, Offset(x, center.y - side * 0.07f), Offset(x, center.y - side * 0.01f), strokeWidth = side * 0.02f)
            }
        }
        2 -> { // Fraccion
            drawLine(ink, Offset(center.x - side * 0.14f, center.y), Offset(center.x + side * 0.14f, center.y), strokeWidth = side * 0.035f)
            drawCircle(ink, side * 0.055f, Offset(center.x, center.y - side * 0.11f))
            drawCircle(ink, side * 0.055f, Offset(center.x, center.y + side * 0.11f))
        }
        3 -> { // Reloj
            drawCircle(ink, side * 0.17f, center, style = Stroke(width = side * 0.035f))
            drawLine(ink, center, Offset(center.x, center.y - side * 0.11f), strokeWidth = side * 0.032f)
            drawLine(ink, center, Offset(center.x + side * 0.09f, center.y), strokeWidth = side * 0.028f)
        }
        4 -> { // Triangulo (geometria)
            drawPath(polygonPath(center, side * 0.18f, 3, 0f), ink)
        }
        5 -> { // Cristal
            drawPath(polygonPath(center, side * 0.17f, 5, 12f), ink)
            drawPath(polygonPath(center, side * 0.17f, 5, 12f), hue, style = Stroke(width = side * 0.02f))
        }
        else -> { // Rayo de energia
            val bolt = Path().apply {
                moveTo(center.x + side * 0.04f, center.y - side * 0.19f)
                lineTo(center.x - side * 0.10f, center.y + side * 0.02f)
                lineTo(center.x - side * 0.01f, center.y + side * 0.02f)
                lineTo(center.x - side * 0.05f, center.y + side * 0.19f)
                lineTo(center.x + side * 0.11f, center.y - side * 0.04f)
                lineTo(center.x + side * 0.01f, center.y - side * 0.04f)
                close()
            }
            drawPath(bolt, ink)
        }
    }

    if (!unlocked) {
        drawCircle(Deep.copy(alpha = 0.10f), side * 0.36f, center)
    }
}
