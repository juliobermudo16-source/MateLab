package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.Fraction
import com.matelab.islas.domain.engine.FractionEngine
import com.matelab.islas.domain.model.FractionLinePayload
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal

/**
 * Recta numerica.
 *
 * El nino arrastra la ficha hasta la marca correcta. La ficha se imanta a las
 * marcas para que colocarla sea preciso pero no frustrante.
 */
@Composable
fun FractionLineGame(
    payload: FractionLinePayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val steps = payload.denominator * payload.wholes
    var step by remember { mutableIntStateOf(0) }

    val target = if (payload.decimalLabels) {
        decimalLabel(payload.numerator, payload.denominator)
    } else {
        "${payload.numerator}/${payload.denominator}"
    }

    val outline = MateTheme.colors.outline
    val inkSoft = MateTheme.colors.inkSoft

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Teal.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "Coloca la ficha en $target",
                style = MaterialTheme.typography.titleMedium,
                color = MateTheme.colors.ink
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MateTheme.colors.cardAlt)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .pointerInput(enabled, steps) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val position = (change.position.x / size.width).toDouble()
                            step = FractionEngine.snapToStep(position, payload.denominator, payload.wholes)
                                .coerceIn(0, steps)
                        }
                    }
                    .pointerInput(enabled, steps) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { tap ->
                            val position = (tap.x / size.width).toDouble()
                            step = FractionEngine.snapToStep(position, payload.denominator, payload.wholes)
                                .coerceIn(0, steps)
                            feedback.tap()
                        }
                    }
            ) {
                val lineY = size.height * 0.62f
                val w = size.width

                // Recta
                drawLine(Deep, Offset(0f, lineY), Offset(w, lineY), strokeWidth = 6f)

                // Marcas
                for (i in 0..steps) {
                    val x = w * i / steps.toFloat()
                    val whole = i % payload.denominator == 0
                    drawLine(
                        if (whole) Deep else outline,
                        Offset(x, lineY - (if (whole) 26f else 14f)),
                        Offset(x, lineY + (if (whole) 26f else 14f)),
                        strokeWidth = if (whole) 6f else 3f
                    )
                    if (whole) {
                        drawCircle(Sun, 7f, Offset(x, lineY))
                    }
                }

                // Ficha arrastrable
                val x = w * step / steps.toFloat()
                drawLine(Coral.copy(alpha = 0.5f), Offset(x, lineY - 60f), Offset(x, lineY), strokeWidth = 4f)
                drawCircle(Coral, 24f, Offset(x, lineY - 62f))
                drawCircle(Color.White, 11f, Offset(x, lineY - 62f))
                drawCircle(Sun, 6f, Offset(x, lineY - 62f))
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (whole in 0..payload.wholes) {
                    Text(
                        text = whole.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = inkSoft
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        val currentLabel = if (payload.decimalLabels) {
            decimalLabel(step, payload.denominator)
        } else {
            FractionEngine.mixedLabel(Fraction(step, payload.denominator))
        }
        Text(
            "Ficha en: $currentLabel",
            style = MaterialTheme.typography.headlineSmall,
            color = MateTheme.colors.ink
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth()) {
            GhostButton(
                text = "Atras",
                onClick = {
                    step = (step - 1).coerceAtLeast(0)
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.padding(horizontal = 5.dp))
            GhostButton(
                text = "Adelante",
                onClick = {
                    step = (step + 1).coerceAtMost(steps)
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(10.dp))

        MateButton(
            text = "Comprobar",
            onClick = {
                onSubmit(FractionEngine.checkLine(step, payload.numerator, payload.toleranceSteps))
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 7/10 se muestra como 0,7 cuando la recta va en decimales. */
internal fun decimalLabel(numerator: Int, denominator: Int): String {
    if (denominator == 0) return "-"
    val value = numerator.toDouble() / denominator
    val rounded = Math.round(value * 100.0) / 100.0
    return rounded.toString().replace('.', ',')
}
