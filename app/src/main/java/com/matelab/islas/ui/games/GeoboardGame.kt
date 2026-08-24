package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.GeoboardEngine
import com.matelab.islas.domain.engine.GridPoint
import com.matelab.islas.domain.model.GeoObjective
import com.matelab.islas.domain.model.GeoboardPayload
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlin.math.roundToInt

/**
 * Geoplano interactivo.
 *
 * El nino toca los clavos para tender gomas y crear un poligono. La app mide
 * area y perimetro de verdad (formula del zapato), no compara con una
 * respuesta escrita a mano.
 */
@Composable
fun GeoboardGame(
    payload: GeoboardPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val points = remember { mutableStateListOf<GridPoint>() }
    val measure by remember {
        derivedStateOf { GeoboardEngine.measure(points.toList()) }
    }
    val grid = payload.grid.coerceIn(3, 10)

    Column(modifier.fillMaxWidth()) {

        ObjectiveBanner(payload)

        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MateTheme.colors.cardAlt)
        ) {
            val outline = MateTheme.colors.outline
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(18.dp)
                    .pointerInput(enabled, grid) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { tap ->
                            val step = size.width / grid.toFloat()
                            val gx = (tap.x / step).roundToInt().coerceIn(0, grid)
                            val gy = (tap.y / step).roundToInt().coerceIn(0, grid)
                            val point = GridPoint(gx, gy)
                            if (points.isNotEmpty() && points.last() == point) {
                                points.removeAt(points.size - 1)
                            } else if (point in points) {
                                points.remove(point)
                            } else {
                                points.add(point)
                            }
                            feedback.tap()
                        }
                    }
            ) {
                val step = size.width / grid.toFloat()

                // Cuadricula de fondo
                for (i in 0..grid) {
                    val p = i * step
                    drawLine(outline, Offset(p, 0f), Offset(p, size.height), strokeWidth = 1.2f)
                    drawLine(outline, Offset(0f, p), Offset(size.width, p), strokeWidth = 1.2f)
                }

                // Poligono en construccion
                if (points.size >= 2) {
                    val path = Path()
                    points.forEachIndexed { index, gp ->
                        val x = gp.x * step
                        val y = gp.y * step
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    if (points.size >= 3) path.close()
                    drawPath(path, Teal.copy(alpha = 0.28f))
                    drawPath(path, Teal, style = Stroke(width = 5f))
                }

                // Clavos
                for (x in 0..grid) {
                    for (y in 0..grid) {
                        val selected = GridPoint(x, y) in points
                        drawCircle(
                            color = if (selected) Sun else Deep.copy(alpha = 0.35f),
                            radius = if (selected) 9f else 5f,
                            center = Offset(x * step, y * step)
                        )
                    }
                }

                // Numero de orden del vertice
                points.forEachIndexed { index, gp ->
                    if (index == 0) {
                        drawCircle(
                            Sun,
                            radius = 13f,
                            center = Offset(gp.x * step, gp.y * step),
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        MatePanel(contentPadding = 12.dp) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Readout("Lados", if (points.size >= 3) points.size.toString() else "-")
                Readout("Area", if (measure.valid) trim(measure.area) else "-")
                Readout("Perimetro", if (measure.valid) trim(GeoboardEngine.round2(measure.perimeter)) else "-")
            }
        }

        if (measure.problem != null && points.size >= 2) {
            Spacer(Modifier.height(6.dp))
            Text(
                measure.problem ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MateTheme.colors.warning
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostButton(
                text = "Borrar",
                onClick = {
                    points.clear()
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            MateButton(
                text = "Comprobar",
                onClick = {
                    onSubmit(
                        GeoboardEngine.matchesObjective(points.toList(), payload.objective, payload.target)
                    )
                },
                enabled = enabled && points.size >= payload.minVertices,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
private fun ObjectiveBanner(payload: GeoboardPayload) {
    val label = when (payload.objective) {
        GeoObjective.AREA -> "Area objetivo: ${trim(payload.target)} ${payload.unitLabel}"
        GeoObjective.PERIMETRO -> "Perimetro objetivo: ${trim(payload.target)} ${payload.unitLabel}"
        GeoObjective.LADOS -> "Lados objetivo: ${payload.target.toInt()}"
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Teal.copy(alpha = 0.15f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = MateTheme.colors.ink)
    }
}

@Composable
private fun Readout(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MateTheme.colors.ink)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.inkSoft)
    }
}

internal fun trim(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString().replace('.', ',')
    }
}

internal val GeoboardAccent: Color = Teal
