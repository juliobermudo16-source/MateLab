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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.AngleEngine
import com.matelab.islas.domain.engine.ClockEngine
import com.matelab.islas.domain.engine.ClockTime
import com.matelab.islas.domain.model.ClockMode
import com.matelab.islas.domain.model.ClockPayload
import com.matelab.islas.ui.art.onCircle
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlin.math.hypot

/**
 * Reloj de manecillas arrastrables.
 *
 * Se puede coger la aguja larga o la corta: la app calcula la hora a partir
 * del angulo real de cada manecilla.
 */
@Composable
fun ClockGame(
    payload: ClockPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    var hour by remember { mutableIntStateOf(if (payload.mode == ClockMode.AVANZAR) payload.startHour % 12 else 12 % 12) }
    var minute by remember { mutableIntStateOf(if (payload.mode == ClockMode.AVANZAR) payload.startMinute else 0) }
    var grabbed by remember { mutableStateOf<String?>(null) }

    val expected: ClockTime = remember(payload) {
        when (payload.mode) {
            ClockMode.PONER_HORA -> ClockTime(payload.targetHour % 12, payload.targetMinute)
            ClockMode.AVANZAR -> ClockEngine.addMinutes(
                ClockTime(payload.startHour % 12, payload.startMinute),
                payload.deltaMinutes
            )
        }
    }

    Column(modifier.fillMaxWidth()) {

        if (payload.mode == ClockMode.AVANZAR) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Mango.copy(alpha = 0.15f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    "Salida ${ClockTime(payload.startHour % 12, payload.startMinute).label()}  +  " +
                        ClockEngine.durationLabel(payload.deltaMinutes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MateTheme.colors.ink
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MateTheme.colors.cardAlt)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(18.dp)
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures(
                            onDragStart = { start ->
                                val c = Offset(size.width / 2f, size.height / 2f)
                                val r = size.width * 0.42f
                                val d = hypot(start.x - c.x, start.y - c.y)
                                grabbed = if (d < r * 0.55f) "hora" else "minuto"
                            },
                            onDragEnd = { grabbed = null }
                        ) { change, _ ->
                            change.consume()
                            val c = Offset(size.width / 2f, size.height / 2f)
                            // Angulo de reloj: 0 grados arriba, sentido horario.
                            val dx = (change.position.x - c.x).toDouble()
                            val dy = (change.position.y - c.y).toDouble()
                            val clockDegrees = AngleEngine.normalize(90.0 - AngleEngine.angleOf(dx, dy))
                            if (grabbed == "hora") {
                                hour = ClockEngine.hourFromAngle(clockDegrees)
                            } else {
                                minute = ClockEngine.minuteFromAngle(clockDegrees)
                            }
                        }
                    }
            ) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.width * 0.44f

                drawCircle(Sand, r, c)
                drawCircle(Deep.copy(alpha = 0.7f), r, c, style = Stroke(width = 7f))
                drawCircle(Teal.copy(alpha = 0.10f), r * 0.86f, c)

                for (m in 0 until 60) {
                    val big = m % 5 == 0
                    val outer = onCircle(c, r * 0.94f, m * 6f)
                    val inner = onCircle(c, r * (if (big) 0.84f else 0.89f), m * 6f)
                    drawLine(
                        Deep.copy(alpha = if (big) 0.8f else 0.35f),
                        inner, outer,
                        strokeWidth = if (big) 5f else 2f
                    )
                }

                // Numeros marcados con puntos de color (las horas)
                for (h in 0 until 12) {
                    val p = onCircle(c, r * 0.72f, h * 30f)
                    drawCircle(if (h % 3 == 0) Sun else Teal.copy(alpha = 0.5f), r * 0.045f, p)
                }

                val minuteTip = onCircle(c, r * 0.80f, ClockEngine.minuteHandAngle(minute).toFloat())
                val hourTip = onCircle(c, r * 0.52f, ClockEngine.hourHandAngle(hour, minute).toFloat())

                drawLine(Coral, c, hourTip, strokeWidth = 14f, cap = StrokeCap.Round)
                drawLine(Deep, c, minuteTip, strokeWidth = 9f, cap = StrokeCap.Round)
                drawCircle(Coral, r * 0.10f, hourTip)
                drawCircle(Deep, r * 0.08f, minuteTip)
                drawCircle(Sun, r * 0.07f, c)
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Teal.copy(alpha = 0.14f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                ClockTime(hour, minute).label(),
                style = MaterialTheme.typography.displayMedium,
                color = MateTheme.colors.ink
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GhostButton(
                text = "- 5 min",
                onClick = {
                    val t = ClockEngine.addMinutes(ClockTime(hour, minute), -5)
                    hour = t.hour
                    minute = t.minute
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
            GhostButton(
                text = "+ 5 min",
                onClick = {
                    val t = ClockEngine.addMinutes(ClockTime(hour, minute), 5)
                    hour = t.hour
                    minute = t.minute
                    feedback.tap()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        MateButton(
            text = "Comprobar",
            onClick = { onSubmit(ClockEngine.matches(ClockTime(hour, minute), expected, 0)) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
