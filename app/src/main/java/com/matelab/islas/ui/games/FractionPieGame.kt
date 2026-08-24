package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.Fraction
import com.matelab.islas.domain.engine.FractionEngine
import com.matelab.islas.domain.model.FractionMode
import com.matelab.islas.domain.model.FractionPiePayload
import com.matelab.islas.domain.model.PieShape
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Reparto en partes iguales.
 *
 * El nino toca las porciones de una tarta o de una barra para pintar la
 * fraccion pedida. La comprobacion usa equivalencia real de fracciones.
 */
@Composable
fun FractionPieGame(
    payload: FractionPiePayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val selected = remember { mutableStateListOf<Int>() }
    val parts = payload.parts.coerceIn(2, 16)

    fun toggle(index: Int) {
        if (!enabled) return
        if (index in selected) selected.remove(index) else selected.add(index)
        feedback.tap()
    }

    Column(modifier.fillMaxWidth()) {

        if (payload.mode == FractionMode.EQUIVALENTE) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Coral.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    "Objetivo: ${payload.targetNumerator}/${payload.targetDenominator}  " +
                        "usando ${parts} partes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MateTheme.colors.ink
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        when (payload.shape) {
            PieShape.CIRCULO -> PieCircle(parts, selected, enabled, ::toggle)
            PieShape.BARRA -> PieBar(parts, selected, enabled, ::toggle)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FractionBadge(selected.size, parts)
            Spacer(Modifier.size(12.dp))
            Text(
                "Has pintado ${selected.size} de $parts partes",
                style = MaterialTheme.typography.bodyMedium,
                color = MateTheme.colors.inkSoft
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            GhostButton(
                text = "Limpiar",
                onClick = {
                    selected.clear()
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(10.dp))
            MateButton(
                text = "Comprobar",
                onClick = {
                    onSubmit(
                        FractionEngine.checkPainted(
                            painted = selected.size,
                            parts = parts,
                            targetNumerator = payload.targetNumerator,
                            targetDenominator = payload.targetDenominator
                        )
                    )
                },
                enabled = enabled && selected.isNotEmpty(),
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
private fun PieCircle(
    parts: Int,
    selected: List<Int>,
    enabled: Boolean,
    onToggle: (Int) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(MateTheme.colors.cardAlt)
            .padding(18.dp)
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(enabled, parts) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { tap ->
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val r = size.width * 0.44f
                        val d = hypot(tap.x - c.x, tap.y - c.y)
                        if (d > r) return@detectTapGestures
                        var deg = Math.toDegrees(
                            atan2((tap.y - c.y).toDouble(), (tap.x - c.x).toDouble())
                        ) + 90.0
                        if (deg < 0) deg += 360.0
                        val index = ((deg / 360.0) * parts).toInt().coerceIn(0, parts - 1)
                        onToggle(index)
                    }
                }
        ) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.width * 0.44f
            val sweep = 360f / parts

            drawCircle(Sand, r, c)
            for (i in 0 until parts) {
                if (i in selected) {
                    drawArc(
                        Coral,
                        startAngle = -90f + sweep * i,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(c.x - r, c.y - r),
                        size = Size(r * 2, r * 2)
                    )
                }
            }
            for (i in 0 until parts) {
                val end = com.matelab.islas.ui.art.onCircle(c, r, sweep * i)
                drawLine(Deep.copy(alpha = 0.55f), c, end, strokeWidth = 4f)
            }
            drawCircle(Deep.copy(alpha = 0.7f), r, c, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
        }
    }
}

@Composable
private fun PieBar(
    parts: Int,
    selected: List<Int>,
    enabled: Boolean,
    onToggle: (Int) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MateTheme.colors.cardAlt)
            .padding(horizontal = 16.dp, vertical = 30.dp)
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .pointerInput(enabled, parts) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { tap ->
                        val index = ((tap.x / size.width) * parts).toInt().coerceIn(0, parts - 1)
                        onToggle(index)
                    }
                }
        ) {
            val segment = size.width / parts
            drawRoundRect(
                Sand,
                size = size,
                cornerRadius = CornerRadius(14f, 14f)
            )
            for (i in 0 until parts) {
                if (i in selected) {
                    drawRect(
                        Coral,
                        topLeft = Offset(i * segment, 0f),
                        size = Size(segment, size.height)
                    )
                }
            }
            for (i in 1 until parts) {
                drawLine(
                    Deep.copy(alpha = 0.55f),
                    Offset(i * segment, 0f),
                    Offset(i * segment, size.height),
                    strokeWidth = 4f
                )
            }
            drawRoundRect(
                Deep.copy(alpha = 0.7f),
                size = size,
                cornerRadius = CornerRadius(14f, 14f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
            )
        }
    }
}

/** Fraccion grande dibujada con numerador, raya y denominador. */
@Composable
fun FractionBadge(numerator: Int, denominator: Int, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Sun.copy(alpha = 0.22f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            numerator.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MateTheme.colors.ink
        )
        Box(
            Modifier
                .size(width = 26.dp, height = 3.dp)
                .background(MateTheme.colors.ink)
        )
        Text(
            denominator.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MateTheme.colors.ink
        )
    }
}

/** Etiqueta corta de una fraccion, util en varios sitios. */
fun fractionLabel(numerator: Int, denominator: Int): String =
    if (denominator == 0) "-" else FractionEngine.mixedLabel(Fraction(numerator, denominator))
