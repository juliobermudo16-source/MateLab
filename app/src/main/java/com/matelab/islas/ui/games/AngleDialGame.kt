package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.AngleEngine
import com.matelab.islas.domain.engine.AngleKind
import com.matelab.islas.domain.model.AngleDialPayload
import com.matelab.islas.ui.art.onCircle
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlin.math.roundToInt

/**
 * Transportador giratorio.
 *
 * El nino arrastra el rayo movil para abrir el angulo. El valor se calcula
 * con la posicion real del dedo, no con un deslizador de numeros.
 */
@Composable
fun AngleDialGame(
    payload: AngleDialPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    var angle by remember { mutableFloatStateOf(0f) }
    var classification by remember { mutableStateOf<AngleKind?>(null) }

    val outline = MateTheme.colors.outline
    val cardAlt = MateTheme.colors.cardAlt

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(cardAlt)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp)
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, _ ->
                            change.consume()
                            val c = Offset(size.width / 2f, size.height * 0.78f)
                            val dx = (change.position.x - c.x).toDouble()
                            val dy = (change.position.y - c.y).toDouble()
                            angle = AngleEngine.snap(AngleEngine.angleOf(dx, dy), 1).toFloat()
                        }
                    }
            ) {
                val c = Offset(size.width / 2f, size.height * 0.78f)
                val radius = size.width * 0.42f

                // Semidisco del transportador
                if (payload.showProtractor) {
                    drawArc(
                        color = Teal.copy(alpha = 0.12f),
                        startAngle = 180f, sweepAngle = 180f, useCenter = true,
                        topLeft = Offset(c.x - radius, c.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    for (deg in 0..180 step 10) {
                        val long = deg % 30 == 0
                        val outer = onCircle(c, radius, (deg + 90).toFloat())
                        val inner = onCircle(c, radius * if (long) 0.86f else 0.92f, (deg + 90).toFloat())
                        drawLine(
                            Deep.copy(alpha = if (long) 0.55f else 0.28f),
                            inner, outer,
                            strokeWidth = if (long) 3f else 1.6f
                        )
                    }
                }

                // Circunferencia completa de referencia
                drawCircle(outline, radius, c, style = Stroke(width = 2f))

                // Sector del angulo formado
                drawArc(
                    color = Sun.copy(alpha = 0.35f),
                    startAngle = -angle,
                    sweepAngle = angle,
                    useCenter = true,
                    topLeft = Offset(c.x - radius * 0.55f, c.y - radius * 0.55f),
                    size = Size(radius * 1.1f, radius * 1.1f)
                )

                // Rayo fijo (hacia la derecha)
                drawLine(Deep, c, Offset(c.x + radius, c.y), strokeWidth = 7f)

                // Rayo movil
                val tip = Offset(
                    c.x + radius * kotlin.math.cos(Math.toRadians(-angle.toDouble())).toFloat(),
                    c.y + radius * kotlin.math.sin(Math.toRadians(-angle.toDouble())).toFloat()
                )
                drawLine(Coral, c, tip, strokeWidth = 8f)
                drawCircle(Coral, 18f, tip)
                drawCircle(Sun, 10f, tip)
                drawCircle(Deep, 10f, c)
            }

            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Coral.copy(alpha = 0.16f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "${angle.roundToInt()} grados",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MateTheme.colors.ink
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(-15, -1, 1, 15).forEach { delta ->
                GhostButton(
                    text = if (delta > 0) "+$delta" else "$delta",
                    onClick = {
                        angle = AngleEngine.normalize(angle + delta).toFloat()
                        feedback.tap()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (payload.askClassification) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Y como se llama este angulo?",
                style = MaterialTheme.typography.titleMedium,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AngleKind.AGUDO, AngleKind.RECTO, AngleKind.OBTUSO, AngleKind.LLANO).forEach { kind ->
                    val selected = classification == kind
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) Teal else Teal.copy(alpha = 0.12f))
                            .pointerInput(enabled, kind) {
                                if (!enabled) return@pointerInput
                                androidx.compose.foundation.gestures.detectTapGestures {
                                    classification = kind
                                    feedback.tap()
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            kind.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) androidx.compose.ui.graphics.Color.White
                            else MateTheme.colors.ink
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        MateButton(
            text = "Comprobar",
            onClick = {
                val angleOk = AngleEngine.withinTolerance(
                    angle.toDouble(), payload.targetDegrees.toDouble(), payload.tolerance
                )
                val classOk = if (!payload.askClassification) {
                    true
                } else {
                    classification == AngleEngine.classify(payload.targetDegrees.toDouble())
                }
                onSubmit(angleOk && classOk)
            },
            enabled = enabled && (!payload.askClassification || classification != null),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
