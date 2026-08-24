package com.matelab.islas.ui.games

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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.PlaceValueEngine
import com.matelab.islas.domain.model.PlaceValuePayload
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet

/**
 * Bloques de base diez.
 *
 * Cada pieza se dibuja como lo que representa: un cubito, una barra de diez,
 * una placa de cien y un bloque de mil. El total se calcula sumando piezas.
 */
@Composable
fun PlaceValueGame(
    payload: PlaceValuePayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val counts = remember { mutableStateMapOf<Int, Int>() }
    val pieces = payload.pieces.sortedDescending()
    val total = PlaceValueEngine.valueOf(counts)

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Violet.copy(alpha = 0.14f))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Objetivo: ${payload.target}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MateTheme.colors.inkSoft
                )
                Text(
                    total.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (total == payload.target) Teal else MateTheme.colors.ink
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tablero: una columna por posicion
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MateTheme.colors.cardAlt)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pieces.forEach { piece ->
                val count = counts[piece] ?: 0
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(pieceColor(piece).copy(alpha = 0.12f))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        PlaceValueEngine.pieceName(piece),
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        Modifier
                            .height(74.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(Modifier.fillMaxWidth().height(74.dp)) {
                            drawStack(piece, count, size.width, size.height)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        count.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MateTheme.colors.ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StepperButton("-", enabled && count > 0) {
                            counts[piece] = (count - 1).coerceAtLeast(0)
                            feedback.tap()
                        }
                        StepperButton("+", enabled && count < payload.maxPerPiece) {
                            counts[piece] = (count + 1).coerceAtMost(payload.maxPerPiece)
                            feedback.tap()
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        val missing = PlaceValueEngine.remaining(counts, payload.target)
        Text(
            when {
                missing == 0 -> "Ya tienes el numero exacto"
                missing > 0 -> "Faltan $missing"
                else -> "Te has pasado en ${-missing}"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MateTheme.colors.inkSoft
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            GhostButton(
                text = "Vaciar",
                onClick = {
                    counts.clear()
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(10.dp))
            MateButton(
                text = "Comprobar",
                onClick = { onSubmit(PlaceValueEngine.isCorrect(counts, payload.target)) },
                enabled = enabled && total > 0,
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Teal else MateTheme.colors.locked.copy(alpha = 0.4f))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall, color = Color.White)
    }
}

private fun pieceColor(piece: Int): Color = when (piece) {
    1000 -> Violet
    100 -> Teal
    10 -> Mango
    else -> Sun
}

/** Dibuja las piezas apiladas segun su valor. */
private fun DrawScope.drawStack(piece: Int, count: Int, w: Float, h: Float) {
    val color = pieceColor(piece)
    when (piece) {
        1 -> {
            val s = w * 0.22f
            repeat(count.coerceAtMost(9)) { i ->
                val col = i % 3
                val row = i / 3
                drawRoundRect(
                    color,
                    topLeft = Offset(w * 0.14f + col * (s + 3f), h - (row + 1) * (s + 3f)),
                    size = Size(s, s),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
        10 -> {
            val bw = w * 0.16f
            repeat(count.coerceAtMost(9)) { i ->
                val x = w * 0.06f + i * (bw * 0.55f)
                drawRoundRect(
                    color,
                    topLeft = Offset(x, h * 0.12f),
                    size = Size(bw * 0.5f, h * 0.82f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                for (k in 1 until 10) {
                    drawLine(
                        Deep.copy(alpha = 0.25f),
                        Offset(x, h * 0.12f + h * 0.082f * k),
                        Offset(x + bw * 0.5f, h * 0.12f + h * 0.082f * k),
                        strokeWidth = 1f
                    )
                }
            }
        }
        100 -> {
            val s = w * 0.30f
            repeat(count.coerceAtMost(9)) { i ->
                val col = i % 3
                val row = i / 3
                drawRoundRect(
                    color,
                    topLeft = Offset(w * 0.06f + col * (s + 3f), h - (row + 1) * (s * 0.7f + 3f)),
                    size = Size(s, s * 0.7f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    Deep.copy(alpha = 0.3f),
                    topLeft = Offset(w * 0.06f + col * (s + 3f), h - (row + 1) * (s * 0.7f + 3f)),
                    size = Size(s, s * 0.7f),
                    cornerRadius = CornerRadius(3f, 3f),
                    style = Stroke(width = 1.5f)
                )
            }
        }
        else -> {
            val s = w * 0.42f
            repeat(count.coerceAtMost(4)) { i ->
                val col = i % 2
                val row = i / 2
                val x = w * 0.05f + col * (s + 4f)
                val y = h - (row + 1) * (s + 4f)
                drawRoundRect(color, Offset(x, y), Size(s, s), CornerRadius(4f, 4f))
                drawLine(Deep.copy(alpha = 0.35f), Offset(x, y + s * 0.25f), Offset(x + s, y + s * 0.25f), strokeWidth = 1.5f)
                drawLine(Deep.copy(alpha = 0.35f), Offset(x + s * 0.75f, y), Offset(x + s * 0.75f, y + s), strokeWidth = 1.5f)
            }
        }
    }
}

internal val PlaceValueCell: Dp = 34.dp
