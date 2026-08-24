package com.matelab.islas.ui.art

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.matelab.islas.ui.theme.LocalReducedMotion
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.TealSoft

/**
 * Fondo comun del archipielago: cielo degradado, sol, nubes, islas lejanas
 * y olas en la parte baja. Se usa en casi todas las pantallas para que la
 * app nunca muestre un fondo blanco vacio.
 */
@Composable
fun SeaBackdrop(
    modifier: Modifier = Modifier,
    showHorizon: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MateTheme.colors
    val night = isSystemInDarkTheme()
    val reduced = LocalReducedMotion.current

    val transition = rememberInfiniteTransition(label = "backdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (reduced) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(14000), RepeatMode.Reverse),
        label = "drift"
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(colors.canvasTop, colors.canvasBottom)
                )
            )

            val w = size.width
            val h = size.height

            // Sol o luna
            val star = Offset(w * (0.78f + drift * 0.03f), h * 0.10f)
            drawCircle(
                color = if (night) TealSoft.copy(alpha = 0.20f) else Sun.copy(alpha = 0.30f),
                radius = w * 0.16f,
                center = star
            )
            drawCircle(
                color = if (night) TealSoft.copy(alpha = 0.55f) else Sun,
                radius = w * 0.085f,
                center = star
            )

            if (night) {
                drawStars(w, h)
            } else {
                drawCloud(Offset(w * (0.18f + drift * 0.07f), h * 0.09f), w * 0.13f, Color.White.copy(alpha = 0.75f))
                drawCloud(Offset(w * (0.62f - drift * 0.05f), h * 0.17f), w * 0.10f, Color.White.copy(alpha = 0.55f))
            }

            if (showHorizon) {
                // Islas lejanas
                drawDistantIsland(Offset(w * 0.14f, h * 0.33f), w * 0.16f, colors.seaDeep.copy(alpha = 0.18f))
                drawDistantIsland(Offset(w * 0.80f, h * 0.30f), w * 0.12f, colors.seaDeep.copy(alpha = 0.14f))

                // Olas
                val waveTop = h * 0.88f
                drawPath(
                    wavePath(Rect(0f, waveTop, w, h), waves = 4, amplitude = h * 0.012f, phase = drift * 6f),
                    colors.sea.copy(alpha = 0.28f)
                )
                drawPath(
                    wavePath(Rect(0f, waveTop + h * 0.035f, w, h), waves = 3, amplitude = h * 0.010f, phase = -drift * 5f),
                    colors.sea.copy(alpha = 0.20f)
                )
            }
        }
        content()
    }
}

private fun DrawScope.drawCloud(center: Offset, radius: Float, color: Color) {
    drawCircle(color, radius * 0.62f, center)
    drawCircle(color, radius * 0.46f, Offset(center.x - radius * 0.7f, center.y + radius * 0.16f))
    drawCircle(color, radius * 0.52f, Offset(center.x + radius * 0.72f, center.y + radius * 0.12f))
    drawOval(
        color,
        topLeft = Offset(center.x - radius, center.y + radius * 0.05f),
        size = Size(radius * 2f, radius * 0.62f)
    )
}

private fun DrawScope.drawStars(w: Float, h: Float) {
    val rnd = ArtRandom(7)
    repeat(26) {
        val x = rnd.between(0.02f, 0.98f) * w
        val y = rnd.between(0.02f, 0.45f) * h
        val r = rnd.between(1.0f, 2.6f)
        drawCircle(Color.White.copy(alpha = rnd.between(0.25f, 0.8f)), r, Offset(x, y))
    }
}

private fun DrawScope.drawDistantIsland(base: Offset, width: Float, color: Color) {
    val path = Path().apply {
        moveTo(base.x - width, base.y)
        quadraticBezierTo(base.x - width * 0.5f, base.y - width * 0.75f, base.x, base.y - width * 0.5f)
        quadraticBezierTo(base.x + width * 0.45f, base.y - width * 0.95f, base.x + width, base.y)
        close()
    }
    drawPath(path, color)
}
