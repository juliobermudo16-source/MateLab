package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.matelab.islas.domain.engine.SymmetryEngine
import com.matelab.islas.domain.model.SymmetryAxis
import com.matelab.islas.domain.model.SymmetryPayload
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.Violet

/**
 * Taller de mosaicos simetricos.
 *
 * La mitad de partida viene dada y el nino pinta la otra mitad. Se puede
 * pintar arrastrando el dedo, como con un pincel.
 */
@Composable
fun SymmetryGame(
    payload: SymmetryPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val painted = remember { mutableStateListOf<Int>() }
    val rows = payload.rows
    val cols = payload.cols
    val given = payload.given

    val gridColor = MateTheme.colors.outline
    val emptyColor = MateTheme.colors.cardAlt

    fun cellAt(position: Offset, cellW: Float, cellH: Float): Int? {
        val c = (position.x / cellW).toInt()
        val r = (position.y / cellH).toInt()
        if (r !in 0 until rows || c !in 0 until cols) return null
        return r * cols + c
    }

    fun paint(index: Int) {
        if (!enabled) return
        if (!SymmetryEngine.isEditable(index, rows, cols, payload.axis, given)) return
        if (index !in painted) {
            painted.add(index)
            feedback.tap()
        }
    }

    Column(modifier.fillMaxWidth()) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (payload.axis == SymmetryAxis.VERTICAL) "Eje vertical" else "Eje horizontal",
                style = MaterialTheme.typography.labelMedium,
                color = MateTheme.colors.inkSoft
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(MateTheme.colors.card)
                .padding(10.dp)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(enabled, rows, cols) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { position ->
                            val cellW = size.width / cols.toFloat()
                            val cellH = size.height / rows.toFloat()
                            val index = cellAt(position, cellW, cellH) ?: return@detectTapGestures
                            if (index in painted) {
                                painted.remove(index)
                                feedback.tap()
                            } else {
                                paint(index)
                            }
                        }
                    }
                    .pointerInput(enabled, rows, cols) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val cellW = size.width / cols.toFloat()
                            val cellH = size.height / rows.toFloat()
                            cellAt(change.position, cellW, cellH)?.let { paint(it) }
                        }
                    }
            ) {
                val cellW = size.width / cols.toFloat()
                val cellH = size.height / rows.toFloat()

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        val color = when {
                            index in given -> Teal
                            index in painted -> Violet
                            SymmetryEngine.isEditable(index, rows, cols, payload.axis, given) -> emptyColor
                            else -> gridColor.copy(alpha = 0.35f)
                        }
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(c * cellW + 2f, r * cellH + 2f),
                            size = Size(cellW - 4f, cellH - 4f),
                            cornerRadius = CornerRadius(cellW * 0.18f, cellW * 0.18f)
                        )
                    }
                }

                // Eje de simetria
                if (payload.axis == SymmetryAxis.VERTICAL) {
                    val x = cellW * (cols / 2f)
                    drawLine(Coral, Offset(x, 0f), Offset(x, size.height), strokeWidth = 6f)
                } else {
                    val y = cellH * (rows / 2f)
                    drawLine(Coral, Offset(0f, y), Offset(size.width, y), strokeWidth = 6f)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        val percent = SymmetryEngine.completionPercent(given, painted, rows, cols, payload.axis)
        Text(
            "Reflejo completado: $percent %",
            style = MaterialTheme.typography.titleMedium,
            color = MateTheme.colors.inkSoft
        )

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth()) {
            GhostButton(
                text = "Limpiar",
                onClick = {
                    painted.clear()
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(0.dp))
            Spacer(Modifier.padding(horizontal = 5.dp))
            MateButton(
                text = "Comprobar",
                onClick = {
                    onSubmit(
                        SymmetryEngine.isComplete(given, painted.toList(), rows, cols, payload.axis)
                    )
                },
                enabled = enabled && painted.isNotEmpty(),
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}
