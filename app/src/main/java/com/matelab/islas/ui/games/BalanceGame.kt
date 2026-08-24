package com.matelab.islas.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.MeasureEngine
import com.matelab.islas.domain.model.BalancePayload
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet
import kotlin.math.abs

/**
 * Balanza de dos platos.
 *
 * El brazo se inclina de verdad segun la diferencia de masa y solo queda
 * horizontal cuando los dos platos pesan exactamente igual.
 */
@Composable
fun BalanceGame(
    payload: BalancePayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val placed = remember { mutableStateListOf<Int>() }

    val diff = MeasureEngine.difference(payload.leftGrams, placed)
    val targetTilt = (-diff.toFloat() / payload.leftGrams.coerceAtLeast(1).toFloat() * 22f)
        .coerceIn(-16f, 16f)
    val tilt by animateFloatAsState(targetTilt, tween(420), label = "tilt")

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MateTheme.colors.cardAlt)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .padding(16.dp)
            ) {
                val cx = size.width / 2f
                val pivotY = size.height * 0.34f

                // Columna y base
                drawRoundRect(
                    Deep,
                    topLeft = Offset(cx - 7f, pivotY),
                    size = Size(14f, size.height * 0.52f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    Deep,
                    topLeft = Offset(cx - size.width * 0.16f, size.height * 0.86f),
                    size = Size(size.width * 0.32f, 14f),
                    cornerRadius = CornerRadius(7f, 7f)
                )

                rotate(degrees = tilt, pivot = Offset(cx, pivotY)) {
                    val armHalf = size.width * 0.33f
                    drawRoundRect(
                        Teal,
                        topLeft = Offset(cx - armHalf, pivotY - 7f),
                        size = Size(armHalf * 2, 14f),
                        cornerRadius = CornerRadius(7f, 7f)
                    )
                    drawPlate(Offset(cx - armHalf, pivotY), size.width * 0.15f, Mango)
                    drawPlate(Offset(cx + armHalf, pivotY), size.width * 0.15f, Violet)

                    // Objeto misterioso en el plato izquierdo
                    drawRoundRect(
                        Sun,
                        topLeft = Offset(cx - armHalf - size.width * 0.06f, pivotY + size.height * 0.10f),
                        size = Size(size.width * 0.12f, size.height * 0.11f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Pesas apiladas en el plato derecho
                    placed.take(8).forEachIndexed { index, w ->
                        val row = index / 2
                        val col = index % 2
                        val wd = size.width * 0.055f + (w / 1000f) * size.width * 0.02f
                        drawRoundRect(
                            weightColor(w),
                            topLeft = Offset(
                                cx + armHalf - size.width * 0.06f + col * (wd + 4f),
                                pivotY + size.height * 0.16f - row * (size.height * 0.05f)
                            ),
                            size = Size(wd, size.height * 0.045f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }

                drawCircle(Sun, 11f, Offset(cx, pivotY))
            }

            Text(
                payload.leftLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MateTheme.colors.ink,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            )
            Text(
                "${placed.sum()} g",
                style = MaterialTheme.typography.headlineSmall,
                color = MateTheme.colors.ink,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        val message = when {
            diff == 0 && placed.isNotEmpty() -> "La balanza esta equilibrada"
            diff > 0 -> "Faltan $diff g en el plato derecho"
            diff < 0 -> "Te has pasado en ${abs(diff)} g"
            else -> "Anade pesas al plato derecho"
        }
        Text(message, style = MaterialTheme.typography.titleMedium, color = MateTheme.colors.inkSoft)

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            payload.weights.forEach { w ->
                val used = placed.count { it == w }
                val canAdd = used < payload.maxPerWeight
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (canAdd) weightColor(w).copy(alpha = 0.85f)
                            else MateTheme.colors.locked.copy(alpha = 0.4f)
                        )
                        .pointerInput(enabled, w, canAdd) {
                            if (!enabled || !canAdd) return@pointerInput
                            detectTapGestures {
                                placed.add(w)
                                feedback.tap()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (w >= 1000) "${w / 1000} kg" else "$w g",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "x$used",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            GhostButton(
                text = "Quitar",
                onClick = {
                    if (placed.isNotEmpty()) {
                        placed.removeAt(placed.size - 1)
                        feedback.tap()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(10.dp))
            MateButton(
                text = "Comprobar",
                onClick = { onSubmit(MeasureEngine.isBalanced(payload.leftGrams, placed.toList())) },
                enabled = enabled && placed.isNotEmpty(),
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

private fun weightColor(grams: Int): Color = when {
    grams >= 1000 -> Violet
    grams >= 500 -> Teal
    grams >= 200 -> Mango
    grams >= 100 -> Sun
    else -> Color(0xFF7ED957)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlate(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawLine(Deep, center, Offset(center.x, center.y + radius * 0.7f), strokeWidth = 4f)
    drawArc(
        color,
        startAngle = 0f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(center.x - radius, center.y + radius * 0.4f),
        size = Size(radius * 2, radius * 0.9f)
    )
    drawLine(
        Sand,
        Offset(center.x - radius, center.y + radius * 0.7f),
        Offset(center.x + radius, center.y + radius * 0.7f),
        strokeWidth = 5f
    )
}
