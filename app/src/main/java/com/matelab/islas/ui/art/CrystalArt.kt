package com.matelab.islas.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.model.Rarity
import com.matelab.islas.ui.theme.Aqua
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Lime
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet

private val CrystalHues = listOf(Teal, Mango, Violet, Sun, Coral, Lime, Aqua)

/**
 * Cristal de Ingenio dibujado de forma parametrica.
 *
 * La semilla decide caras, inclinacion y color, asi que los 27 cristales de
 * la coleccion se ven claramente distintos entre si sin necesidad de 27
 * ficheros de imagen.
 */
@Composable
fun CrystalArt(
    artSeed: Int,
    rarity: Rarity,
    unlocked: Boolean,
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            drawCrystal(this.size.width, artSeed, rarity, unlocked)
        }
    }
}

private fun DrawScope.drawCrystal(
    side: Float,
    artSeed: Int,
    rarity: Rarity,
    unlocked: Boolean
) {
    val rnd = ArtRandom(artSeed * 31 + 7)
    val center = Offset(side / 2f, side / 2f)
    val radius = side * 0.36f
    val faces = 5 + (artSeed % 4)
    val tilt = rnd.between(-18f, 18f)
    val hue = CrystalHues[artSeed % CrystalHues.size]
    val hue2 = CrystalHues[(artSeed * 3 + 2) % CrystalHues.size]

    val base = if (unlocked) hue else Color(0xFF9FB4BA)
    val second = if (unlocked) hue2 else Color(0xFFB9C9CE)

    if (unlocked && rarity != Rarity.COMUN) {
        val glow = if (rarity == Rarity.LEGENDARIO) 0.32f else 0.20f
        drawCircle(base.copy(alpha = glow), radius * 1.55f, center)
    }

    val body = polygonPath(center, radius, faces, tilt)
    drawPath(body, base)

    // Caras internas: dan volumen sin usar sombras costosas
    val top = onCircle(center, radius, tilt)
    for (i in 0 until faces) {
        val a1 = tilt + 360f / faces * i
        val a2 = tilt + 360f / faces * (i + 1)
        if (i % 2 == 0) continue
        val facet = Path().apply {
            moveTo(center.x, center.y - radius * 0.15f)
            val p1 = onCircle(center, radius, a1)
            val p2 = onCircle(center, radius, a2)
            lineTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            close()
        }
        drawPath(facet, second.copy(alpha = 0.55f))
    }

    // Punta superior
    val spike = Path().apply {
        moveTo(top.x, top.y)
        val left = onCircle(center, radius, tilt - 360f / faces)
        val right = onCircle(center, radius, tilt + 360f / faces)
        lineTo(left.x, left.y)
        lineTo(center.x, center.y - radius * 0.05f)
        lineTo(right.x, right.y)
        close()
    }
    drawPath(spike, Color.White.copy(alpha = if (unlocked) 0.30f else 0.15f))

    drawPath(body, Deep.copy(alpha = if (unlocked) 0.55f else 0.30f), style = Stroke(width = side * 0.028f))

    if (unlocked && rarity == Rarity.LEGENDARIO) {
        drawPath(starPath(Offset(side * 0.80f, side * 0.22f), side * 0.075f, side * 0.032f), Sun)
        drawPath(starPath(Offset(side * 0.20f, side * 0.30f), side * 0.05f, side * 0.021f), Color.White)
    }

    if (!unlocked) {
        // Interrogacion sencilla dibujada a mano para el cristal por descubrir
        val q = Path().apply {
            moveTo(center.x - side * 0.05f, center.y - side * 0.06f)
            quadraticBezierTo(center.x + side * 0.09f, center.y - side * 0.14f, center.x + side * 0.03f, center.y + side * 0.01f)
            quadraticBezierTo(center.x - side * 0.01f, center.y + side * 0.05f, center.x, center.y + side * 0.07f)
        }
        drawPath(q, Deep.copy(alpha = 0.6f), style = Stroke(width = side * 0.035f))
        drawCircle(Deep.copy(alpha = 0.6f), side * 0.022f, Offset(center.x, center.y + side * 0.13f))
    }
}
