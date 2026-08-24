package com.matelab.islas.ui.art

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.LocalReducedMotion
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.TealSoft

/** Estados de animo de Kubo, el robot explorador de MateLab. */
enum class KuboMood { NEUTRO, FELIZ, PENSANDO, ANIMANDO, CELEBRANDO }

/**
 * Kubo dibujado con Canvas.
 *
 * Flota suavemente salvo que el nino haya desactivado las animaciones.
 */
@Composable
fun Kubo(
    mood: KuboMood = KuboMood.NEUTRO,
    size: Dp = 96.dp,
    modifier: Modifier = Modifier,
    bodyColor: Color = Sand,
    accent: Color = Teal
) {
    val reduced = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "kubo")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (reduced) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val tilt = when (mood) {
        KuboMood.PENSANDO -> -7f
        KuboMood.CELEBRANDO -> 5f
        else -> 0f
    }

    Box(modifier = modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val u = this.size.width / 100f
            val lift = (bob - 0.5f) * 3f * u
            rotate(degrees = tilt, pivot = Offset(50 * u, 80 * u)) {
                drawKubo(u, lift, mood, bodyColor, accent)
            }
        }
    }
}

private fun DrawScope.drawKubo(
    u: Float,
    lift: Float,
    mood: KuboMood,
    bodyColor: Color,
    accent: Color
) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u + lift)

    // Sombra flotante
    drawOval(
        color = Deep.copy(alpha = 0.14f),
        topLeft = Offset(26 * u, 92 * u),
        size = Size(48 * u, 9 * u)
    )

    // Antena
    val antennaTop = p(50f, 12f)
    drawLine(
        color = Deep,
        start = p(50f, 26f),
        end = antennaTop,
        strokeWidth = 3.2f * u
    )
    val diamond = diamondPath(antennaTop, 7f * u)
    drawPath(diamond, Sun)
    drawPath(diamond, Deep.copy(alpha = 0.85f), style = Stroke(width = 2f * u))

    // Orejas laterales
    drawRoundRectCompat(Mango, p(12f, 46f), Size(10 * u, 18 * u), 4f * u)
    drawRoundRectCompat(Mango, p(78f, 46f), Size(10 * u, 18 * u), 4f * u)

    // Cabeza / cuerpo
    drawRoundRectCompat(bodyColor, p(18f, 26f), Size(64 * u, 58 * u), 18f * u)
    drawRoundRectStrokeCompat(Deep, p(18f, 26f), Size(64 * u, 58 * u), 18f * u, 3f * u)

    // Visor
    drawRoundRectCompat(Deep, p(25f, 36f), Size(50 * u, 30 * u), 13f * u)

    // Ojos segun el animo
    val leftEye = p(39f, 51f)
    val rightEye = p(61f, 51f)
    when (mood) {
        KuboMood.FELIZ, KuboMood.CELEBRANDO -> {
            drawArc(
                color = accent,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(leftEye.x - 8 * u, leftEye.y - 8 * u),
                size = Size(16 * u, 16 * u),
                style = Stroke(width = 4f * u)
            )
            drawArc(
                color = accent,
                startAngle = 200f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(rightEye.x - 8 * u, rightEye.y - 8 * u),
                size = Size(16 * u, 16 * u),
                style = Stroke(width = 4f * u)
            )
        }
        KuboMood.PENSANDO -> {
            drawCircle(accent, radius = 6.5f * u, center = leftEye)
            drawCircle(accent, radius = 3.5f * u, center = rightEye)
            drawCircle(Color.White, radius = 2f * u, center = Offset(leftEye.x - 2 * u, leftEye.y - 2 * u))
        }
        KuboMood.ANIMANDO -> {
            drawCircle(accent, radius = 7.5f * u, center = leftEye)
            drawCircle(accent, radius = 7.5f * u, center = rightEye)
            drawCircle(Color.White, radius = 2.6f * u, center = Offset(leftEye.x - 2.4f * u, leftEye.y - 2.4f * u))
            drawCircle(Color.White, radius = 2.6f * u, center = Offset(rightEye.x - 2.4f * u, rightEye.y - 2.4f * u))
        }
        KuboMood.NEUTRO -> {
            drawCircle(accent, radius = 6f * u, center = leftEye)
            drawCircle(accent, radius = 6f * u, center = rightEye)
            drawCircle(Color.White, radius = 2.1f * u, center = Offset(leftEye.x - 2 * u, leftEye.y - 2 * u))
            drawCircle(Color.White, radius = 2.1f * u, center = Offset(rightEye.x - 2 * u, rightEye.y - 2 * u))
        }
    }

    if (mood == KuboMood.CELEBRANDO) {
        drawPath(starPath(p(20f, 20f), 6f * u, 2.6f * u), Sun)
        drawPath(starPath(p(82f, 24f), 5f * u, 2.2f * u), TealSoft)
    }

    // Boca
    val mouth = Path().apply {
        val a = p(41f, 74f)
        val b = p(59f, 74f)
        moveTo(a.x, a.y)
        when (mood) {
            KuboMood.PENSANDO -> lineTo(b.x, b.y)
            else -> quadraticBezierTo((a.x + b.x) / 2f, a.y + 7 * u, b.x, b.y)
        }
    }
    drawPath(mouth, Deep, style = Stroke(width = 3f * u))
}

/** RoundRect con esquinas iguales, para no repetir el mismo bloque. */
private fun DrawScope.drawRoundRectCompat(
    color: Color,
    topLeft: Offset,
    size: Size,
    radius: Float
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
    )
}

private fun DrawScope.drawRoundRectStrokeCompat(
    color: Color,
    topLeft: Offset,
    size: Size,
    radius: Float,
    strokeWidth: Float
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
        style = Stroke(width = strokeWidth)
    )
}
