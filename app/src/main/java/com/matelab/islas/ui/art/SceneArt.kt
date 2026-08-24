package com.matelab.islas.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet

/**
 * Vinetas ilustradas que acompanan a los retos de eleccion.
 * Evitan que una pregunta sea solo texto y botones.
 */
@Composable
fun SceneArt(
    key: String,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    Box(modifier.fillMaxWidth().height(height)) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height
            when (key) {
                "faro" -> drawSceneFaro(w, h)
                "poligono" -> drawScenePoligono(w, h)
                "espejo" -> drawSceneEspejo(w, h)
                "planos" -> drawScenePlanos(w, h)
                "cinta" -> drawSceneCinta(w, h)
                "balanza" -> drawSceneBalanza(w, h)
                "escalera" -> drawSceneEscalera(w, h)
                "mercado" -> drawSceneMercado(w, h)
                "tarta" -> drawSceneTarta(w, h)
                "cintas" -> drawSceneCintas(w, h)
                "duelo" -> drawSceneDuelo(w, h)
                "rio" -> drawSceneRio(w, h)
                "cueva", "eco" -> drawSceneCueva(w, h)
                else -> drawSceneGenerica(w, h)
            }
        }
    }
}

private fun DrawScope.ground(w: Float, h: Float, color: Color = Teal.copy(alpha = 0.18f)) {
    drawRoundRect(
        color,
        topLeft = Offset(0f, h * 0.78f),
        size = Size(w, h * 0.22f),
        cornerRadius = CornerRadius(h * 0.1f, h * 0.1f)
    )
}

private fun DrawScope.drawSceneFaro(w: Float, h: Float) {
    ground(w, h)
    val baseX = w * 0.30f
    drawPath(
        pathOf(
            listOf(
                Offset(baseX - h * 0.16f, h * 0.80f),
                Offset(baseX - h * 0.10f, h * 0.18f),
                Offset(baseX + h * 0.10f, h * 0.18f),
                Offset(baseX + h * 0.16f, h * 0.80f)
            )
        ),
        Sand
    )
    for (i in 0..2) {
        val y = h * (0.28f + i * 0.17f)
        drawRect(Teal, Offset(baseX - h * 0.13f, y), Size(h * 0.26f, h * 0.08f))
    }
    drawRect(Deep, Offset(baseX - h * 0.12f, h * 0.08f), Size(h * 0.24f, h * 0.11f))
    drawPath(
        pathOf(
            listOf(
                Offset(baseX + h * 0.12f, h * 0.10f),
                Offset(w * 0.95f, h * 0.02f),
                Offset(w * 0.95f, h * 0.32f)
            )
        ),
        Sun.copy(alpha = 0.45f)
    )
    drawPath(polygonPath(Offset(w * 0.72f, h * 0.55f), h * 0.11f, 6), Aqua)
    drawPath(polygonPath(Offset(w * 0.86f, h * 0.68f), h * 0.08f, 3), Mango)
}

private fun DrawScope.drawScenePoligono(w: Float, h: Float) {
    ground(w, h)
    val cx = w * 0.5f
    val cy = h * 0.45f
    val r = h * 0.30f
    val path = polygonPath(Offset(cx, cy), r, 5)
    drawPath(path, Violet.copy(alpha = 0.85f))
    drawPath(path, Deep, style = Stroke(width = h * 0.025f))
    for (i in 0 until 5) {
        val p = onCircle(Offset(cx, cy), r, 360f / 5 * i)
        drawCircle(Sun, h * 0.045f, p)
        drawCircle(Deep, h * 0.045f, p, style = Stroke(width = h * 0.014f))
    }
}

private fun DrawScope.drawSceneEspejo(w: Float, h: Float) {
    val cell = h * 0.16f
    val startX = w * 0.5f - cell * 4
    val startY = h * 0.15f
    val filled = listOf(0 to 2, 1 to 1, 1 to 2, 2 to 0, 2 to 1, 2 to 2, 3 to 2)
    for (r in 0..3) {
        for (c in 0..7) {
            val x = startX + c * cell
            val y = startY + r * cell
            val mirrored = 7 - c
            val on = filled.any { it.first == r && (it.second == c || it.second == mirrored) }
            drawRoundRect(
                if (on) Teal else Sand.copy(alpha = 0.7f),
                Offset(x + 1f, y + 1f),
                Size(cell - 2f, cell - 2f),
                CornerRadius(cell * 0.2f, cell * 0.2f)
            )
        }
    }
    drawLine(
        Coral, Offset(w * 0.5f, startY - cell * 0.3f), Offset(w * 0.5f, startY + cell * 4.3f),
        strokeWidth = h * 0.02f
    )
}

private fun DrawScope.drawScenePlanos(w: Float, h: Float) {
    ground(w, h, Mango.copy(alpha = 0.15f))
    drawRect(Teal.copy(alpha = 0.75f), Offset(w * 0.10f, h * 0.28f), Size(w * 0.30f, h * 0.40f))
    drawRect(Deep, Offset(w * 0.10f, h * 0.28f), Size(w * 0.30f, h * 0.40f), style = Stroke(width = h * 0.02f))
    drawRect(Mango.copy(alpha = 0.75f), Offset(w * 0.55f, h * 0.34f), Size(w * 0.34f, h * 0.28f))
    drawRect(Deep, Offset(w * 0.55f, h * 0.34f), Size(w * 0.34f, h * 0.28f), style = Stroke(width = h * 0.02f))
    for (i in 1..2) {
        drawLine(
            Deep.copy(alpha = 0.35f),
            Offset(w * 0.10f, h * (0.28f + 0.133f * i)),
            Offset(w * 0.40f, h * (0.28f + 0.133f * i)),
            strokeWidth = h * 0.008f
        )
    }
}

private fun DrawScope.drawSceneCinta(w: Float, h: Float) {
    ground(w, h)
    drawRoundRect(
        Sand, Offset(w * 0.08f, h * 0.38f), Size(w * 0.84f, h * 0.22f),
        CornerRadius(h * 0.04f, h * 0.04f)
    )
    for (i in 0..16) {
        val x = w * 0.08f + (w * 0.84f / 16f) * i
        val long = i % 2 == 0
        drawLine(
            Deep, Offset(x, h * 0.38f),
            Offset(x, h * 0.38f + if (long) h * 0.11f else h * 0.06f),
            strokeWidth = h * 0.012f
        )
    }
    drawRoundRect(
        Coral.copy(alpha = 0.85f), Offset(w * 0.14f, h * 0.20f), Size(w * 0.42f, h * 0.12f),
        CornerRadius(h * 0.06f, h * 0.06f)
    )
}

private fun DrawScope.drawSceneBalanza(w: Float, h: Float) {
    ground(w, h)
    val cx = w * 0.5f
    drawRect(Deep, Offset(cx - w * 0.012f, h * 0.30f), Size(w * 0.024f, h * 0.48f))
    drawLine(Deep, Offset(w * 0.20f, h * 0.34f), Offset(w * 0.80f, h * 0.26f), strokeWidth = h * 0.03f)
    drawRoundRect(Mango, Offset(w * 0.13f, h * 0.36f), Size(w * 0.16f, h * 0.09f), CornerRadius(h * 0.03f, h * 0.03f))
    drawRoundRect(Teal, Offset(w * 0.71f, h * 0.28f), Size(w * 0.16f, h * 0.09f), CornerRadius(h * 0.03f, h * 0.03f))
    drawCircle(Violet, h * 0.07f, Offset(w * 0.21f, h * 0.29f))
    drawRoundRect(Sun, Offset(w * 0.75f, h * 0.19f), Size(w * 0.08f, h * 0.08f), CornerRadius(h * 0.02f, h * 0.02f))
}

private fun DrawScope.drawSceneEscalera(w: Float, h: Float) {
    ground(w, h)
    val labels = 5
    for (i in 0 until labels) {
        val x = w * (0.08f + 0.17f * i)
        val y = h * (0.66f - 0.12f * i)
        drawRoundRect(
            listOf(Teal, Aqua, Sun, Mango, Coral)[i].copy(alpha = 0.85f),
            Offset(x, y), Size(w * 0.15f, h * 0.14f),
            CornerRadius(h * 0.03f, h * 0.03f)
        )
    }
}

private fun DrawScope.drawSceneMercado(w: Float, h: Float) {
    ground(w, h, Mango.copy(alpha = 0.18f))
    // Toldo
    for (i in 0..4) {
        val x = w * (0.08f + 0.17f * i)
        drawPath(
            pathOf(
                listOf(
                    Offset(x, h * 0.12f),
                    Offset(x + w * 0.17f, h * 0.12f),
                    Offset(x + w * 0.17f, h * 0.28f),
                    Offset(x, h * 0.28f)
                )
            ),
            if (i % 2 == 0) Coral else Sand
        )
    }
    drawRect(Deep.copy(alpha = 0.8f), Offset(w * 0.08f, h * 0.28f), Size(w * 0.85f, h * 0.03f))
    // Cestas
    drawCircle(Sun, h * 0.10f, Offset(w * 0.28f, h * 0.55f))
    drawCircle(Teal, h * 0.12f, Offset(w * 0.52f, h * 0.58f))
    drawCircle(Violet, h * 0.09f, Offset(w * 0.74f, h * 0.56f))
}

private fun DrawScope.drawSceneTarta(w: Float, h: Float) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val r = h * 0.36f
    drawCircle(Sand, r, center)
    val parts = 7
    for (i in 0 until parts) {
        if (i >= 3) continue
        drawArc(
            Coral.copy(alpha = 0.85f),
            startAngle = -90f + 360f / parts * i,
            sweepAngle = 360f / parts,
            useCenter = true,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(r * 2, r * 2)
        )
    }
    for (i in 0 until parts) {
        val p = onCircle(center, r, 360f / parts * i)
        drawLine(Deep, center, p, strokeWidth = h * 0.014f)
    }
    drawCircle(Deep, r, center, style = Stroke(width = h * 0.022f))
}

private fun DrawScope.drawSceneCintas(w: Float, h: Float) {
    val barH = h * 0.16f
    listOf(2 to 4, 3 to 6, 1 to 2).forEachIndexed { row, pair ->
        val (num, den) = pair
        val y = h * (0.16f + row * 0.26f)
        val width = w * 0.80f
        drawRoundRect(Sand, Offset(w * 0.10f, y), Size(width, barH), CornerRadius(barH * 0.3f, barH * 0.3f))
        for (i in 0 until num) {
            drawRect(
                listOf(Teal, Violet, Mango)[row].copy(alpha = 0.85f),
                Offset(w * 0.10f + width / den * i, y),
                Size(width / den, barH)
            )
        }
        for (i in 1 until den) {
            drawLine(
                Deep.copy(alpha = 0.5f),
                Offset(w * 0.10f + width / den * i, y),
                Offset(w * 0.10f + width / den * i, y + barH),
                strokeWidth = h * 0.008f
            )
        }
    }
}

private fun DrawScope.drawSceneDuelo(w: Float, h: Float) {
    ground(w, h)
    drawCircle(Teal.copy(alpha = 0.85f), h * 0.24f, Offset(w * 0.28f, h * 0.45f))
    drawArc(
        Sand, -90f, 216f, true,
        topLeft = Offset(w * 0.28f - h * 0.24f, h * 0.45f - h * 0.24f),
        size = Size(h * 0.48f, h * 0.48f)
    )
    drawCircle(Deep, h * 0.24f, Offset(w * 0.28f, h * 0.45f), style = Stroke(width = h * 0.02f))

    drawCircle(Coral.copy(alpha = 0.85f), h * 0.24f, Offset(w * 0.72f, h * 0.45f))
    drawArc(
        Sand, -90f, 180f, true,
        topLeft = Offset(w * 0.72f - h * 0.24f, h * 0.45f - h * 0.24f),
        size = Size(h * 0.48f, h * 0.48f)
    )
    drawCircle(Deep, h * 0.24f, Offset(w * 0.72f, h * 0.45f), style = Stroke(width = h * 0.02f))

    drawPath(diamondPath(Offset(w * 0.5f, h * 0.45f), h * 0.09f), Sun)
}

private fun DrawScope.drawSceneRio(w: Float, h: Float) {
    drawPath(
        Path().apply {
            moveTo(0f, h * 0.55f)
            quadraticBezierTo(w * 0.25f, h * 0.35f, w * 0.5f, h * 0.55f)
            quadraticBezierTo(w * 0.75f, h * 0.75f, w, h * 0.50f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        },
        Mango.copy(alpha = 0.55f)
    )
    for (i in 0..10) {
        val x = w * (0.05f + 0.09f * i)
        drawLine(Deep.copy(alpha = 0.45f), Offset(x, h * 0.20f), Offset(x, h * 0.30f), strokeWidth = h * 0.012f)
    }
    drawLine(Deep, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = h * 0.02f)
    drawCircle(Coral, h * 0.06f, Offset(w * 0.32f, h * 0.25f))
}

private fun DrawScope.drawSceneCueva(w: Float, h: Float) {
    drawRoundRect(Deep.copy(alpha = 0.85f), Offset(0f, 0f), Size(w, h), CornerRadius(h * 0.12f, h * 0.12f))
    drawPath(
        Path().apply {
            moveTo(w * 0.30f, h)
            quadraticBezierTo(w * 0.30f, h * 0.25f, w * 0.50f, h * 0.25f)
            quadraticBezierTo(w * 0.70f, h * 0.25f, w * 0.70f, h)
            close()
        },
        Color(0xFF061A22)
    )
    drawCrystalShardScene(Offset(w * 0.18f, h * 0.82f), h * 0.40f, Aqua)
    drawCrystalShardScene(Offset(w * 0.84f, h * 0.86f), h * 0.32f, Violet)
    drawCrystalShardScene(Offset(w * 0.50f, h * 0.92f), h * 0.28f, Sun)
}

private fun DrawScope.drawCrystalShardScene(base: Offset, height: Float, color: Color) {
    val wd = height * 0.36f
    drawPath(
        pathOf(
            listOf(
                base,
                Offset(base.x - wd, base.y - height * 0.5f),
                Offset(base.x - wd * 0.2f, base.y - height),
                Offset(base.x + wd * 0.75f, base.y - height * 0.55f)
            )
        ),
        color
    )
}

private fun DrawScope.drawSceneGenerica(w: Float, h: Float) {
    ground(w, h)
    drawPath(polygonPath(Offset(w * 0.3f, h * 0.45f), h * 0.20f, 3), Teal)
    drawPath(polygonPath(Offset(w * 0.55f, h * 0.42f), h * 0.18f, 6), Mango)
    drawCircle(Violet, h * 0.16f, Offset(w * 0.76f, h * 0.48f))
}
