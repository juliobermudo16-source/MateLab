package com.matelab.islas.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

/** Nombres de los ocho ayudantes que el nino puede elegir. */
val AvatarNames = listOf(
    "Kubo", "Tesela", "Nono", "Vela",
    "Chispa", "Grafo", "Ada", "Duna"
)

private val AvatarColors = listOf(Teal, Mango, Violet, Sun, Coral, Lime, Aqua, Color(0xFF4FA3E3))

/**
 * Ocho avatares locales, dibujados de forma parametrica.
 * No se pide ninguna foto ni ningun dato personal para elegir uno.
 */
@Composable
fun AvatarArt(
    avatarId: Int,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier,
    background: Boolean = true
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            drawAvatar(this.size.width, avatarId.coerceIn(0, AvatarNames.size - 1), background)
        }
    }
}

private fun DrawScope.drawAvatar(side: Float, id: Int, background: Boolean) {
    val color = AvatarColors[id % AvatarColors.size]
    val center = Offset(side / 2f, side / 2f)

    if (background) {
        drawCircle(color.copy(alpha = 0.20f), side * 0.48f, center)
    }

    // Antena: cada avatar lleva una distinta
    when (id % 4) {
        0 -> {
            drawLine(Deep, Offset(center.x, side * 0.24f), Offset(center.x, side * 0.12f), strokeWidth = side * 0.045f)
            drawCircle(Sun, side * 0.065f, Offset(center.x, side * 0.10f))
        }
        1 -> {
            drawLine(Deep, Offset(center.x, side * 0.24f), Offset(center.x, side * 0.12f), strokeWidth = side * 0.045f)
            drawPath(diamondPath(Offset(center.x, side * 0.10f), side * 0.075f), Sun)
        }
        2 -> {
            drawLine(Deep, Offset(side * 0.34f, side * 0.22f), Offset(side * 0.26f, side * 0.10f), strokeWidth = side * 0.04f)
            drawLine(Deep, Offset(side * 0.66f, side * 0.22f), Offset(side * 0.74f, side * 0.10f), strokeWidth = side * 0.04f)
            drawCircle(Sun, side * 0.05f, Offset(side * 0.26f, side * 0.09f))
            drawCircle(Sun, side * 0.05f, Offset(side * 0.74f, side * 0.09f))
        }
        else -> {
            drawPath(starPath(Offset(center.x, side * 0.12f), side * 0.09f, side * 0.038f), Sun)
        }
    }

    // Cabeza
    val headTop = side * 0.24f
    val headSize = Size(side * 0.62f, side * 0.54f)
    val headLeft = Offset(side * 0.19f, headTop)
    val radius = if (id % 2 == 0) side * 0.20f else side * 0.10f
    drawRoundRect(Sand, headLeft, headSize, CornerRadius(radius, radius))
    drawRoundRect(Deep, headLeft, headSize, CornerRadius(radius, radius), style = Stroke(width = side * 0.035f))

    // Visor
    val visorLeft = Offset(side * 0.25f, side * 0.34f)
    val visorSize = Size(side * 0.50f, side * 0.24f)
    drawRoundRect(Deep, visorLeft, visorSize, CornerRadius(side * 0.10f, side * 0.10f))

    // Ojos
    val eyeY = side * 0.46f
    when (id % 3) {
        0 -> {
            drawCircle(color, side * 0.055f, Offset(side * 0.38f, eyeY))
            drawCircle(color, side * 0.055f, Offset(side * 0.62f, eyeY))
        }
        1 -> {
            drawArc(
                color, 200f, 140f, false,
                topLeft = Offset(side * 0.31f, eyeY - side * 0.07f),
                size = Size(side * 0.14f, side * 0.14f),
                style = Stroke(width = side * 0.035f)
            )
            drawArc(
                color, 200f, 140f, false,
                topLeft = Offset(side * 0.55f, eyeY - side * 0.07f),
                size = Size(side * 0.14f, side * 0.14f),
                style = Stroke(width = side * 0.035f)
            )
        }
        else -> {
            drawRoundRect(
                color,
                Offset(side * 0.33f, eyeY - side * 0.045f),
                Size(side * 0.10f, side * 0.09f),
                CornerRadius(side * 0.02f, side * 0.02f)
            )
            drawRoundRect(
                color,
                Offset(side * 0.57f, eyeY - side * 0.045f),
                Size(side * 0.10f, side * 0.09f),
                CornerRadius(side * 0.02f, side * 0.02f)
            )
        }
    }

    // Cuello / hombros
    drawRoundRect(
        color,
        Offset(side * 0.28f, side * 0.78f),
        Size(side * 0.44f, side * 0.14f),
        CornerRadius(side * 0.07f, side * 0.07f)
    )
}
