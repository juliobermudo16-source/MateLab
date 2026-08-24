package com.matelab.islas.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.LengthUnit
import com.matelab.islas.domain.engine.MeasureEngine
import com.matelab.islas.domain.model.RulerPayload
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.Coral
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.Mango
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sand
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlin.math.abs
import kotlin.math.roundToInt

private const val RULER_MM = 160

/**
 * Regla arrastrable.
 *
 * El nino mueve la regla hasta alinear el cero con el objeto y despues toca
 * la marca donde termina. Si no alinea el cero, la lectura no cuadra: ese es
 * justo el error que el reto quiere corregir.
 */
@Composable
fun RulerGame(
    payload: RulerPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    var rulerShift by remember { mutableFloatStateOf(0f) }
    var readingMm by remember { mutableIntStateOf(-1) }

    val unit = if (payload.answerUnit == "mm") LengthUnit.MM else LengthUnit.CM
    val cardAlt = MateTheme.colors.cardAlt

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(cardAlt)
        ) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectDragGestures { change, amount ->
                            change.consume()
                            rulerShift = (rulerShift + amount.x).coerceIn(-size.width * 0.35f, size.width * 0.55f)
                        }
                    }
                    .pointerInput(enabled) {
                        if (!enabled) return@pointerInput
                        detectTapGestures { tap ->
                            val pxPerMm = size.width * 0.92f / RULER_MM
                            val rulerLeft = size.width * 0.04f + rulerShift
                            val rulerTop = size.height * 0.52f
                            if (tap.y < rulerTop - 20f) return@detectTapGestures
                            val mm = ((tap.x - rulerLeft) / pxPerMm).roundToInt()
                            if (mm in 0..RULER_MM) {
                                readingMm = mm
                                feedback.tap()
                            }
                        }
                    }
            ) {
                val pxPerMm = size.width * 0.92f / RULER_MM
                val objectLeft = size.width * 0.12f
                val objectTop = size.height * 0.16f

                drawMeasuredObject(
                    kind = payload.objectKind,
                    left = objectLeft,
                    top = objectTop,
                    lengthPx = payload.objectMm * pxPerMm,
                    height = size.height * 0.22f
                )

                // Guias de los extremos del objeto
                drawLine(
                    Coral.copy(alpha = 0.45f),
                    Offset(objectLeft, objectTop - 10f),
                    Offset(objectLeft, size.height),
                    strokeWidth = 2f
                )
                drawLine(
                    Coral.copy(alpha = 0.45f),
                    Offset(objectLeft + payload.objectMm * pxPerMm, objectTop - 10f),
                    Offset(objectLeft + payload.objectMm * pxPerMm, size.height),
                    strokeWidth = 2f
                )

                // Regla
                val rulerLeft = size.width * 0.04f + rulerShift
                val rulerTop = size.height * 0.52f
                val rulerHeight = size.height * 0.38f
                drawRoundRect(
                    Sand,
                    topLeft = Offset(rulerLeft - 8f, rulerTop),
                    size = Size(RULER_MM * pxPerMm + 16f, rulerHeight),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    Deep.copy(alpha = 0.5f),
                    topLeft = Offset(rulerLeft - 8f, rulerTop),
                    size = Size(RULER_MM * pxPerMm + 16f, rulerHeight),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 2.5f)
                )

                for (mm in 0..RULER_MM) {
                    val x = rulerLeft + mm * pxPerMm
                    val isCm = mm % 10 == 0
                    val isHalf = mm % 5 == 0
                    val len = when {
                        isCm -> rulerHeight * 0.42f
                        isHalf -> rulerHeight * 0.28f
                        else -> rulerHeight * 0.16f
                    }
                    drawLine(
                        Deep.copy(alpha = if (isCm) 0.85f else 0.45f),
                        Offset(x, rulerTop),
                        Offset(x, rulerTop + len),
                        strokeWidth = if (isCm) 3f else 1.5f
                    )
                    if (isCm && mm % 20 == 0) {
                        drawCircle(Teal, 4f, Offset(x, rulerTop + rulerHeight * 0.72f))
                    }
                }

                // Marca del cero
                drawCircle(Sun, 9f, Offset(rulerLeft, rulerTop + rulerHeight * 0.9f))

                // Lectura elegida
                if (readingMm >= 0) {
                    val x = rulerLeft + readingMm * pxPerMm
                    drawLine(Teal, Offset(x, rulerTop - 22f), Offset(x, rulerTop + rulerHeight), strokeWidth = 5f)
                    drawCircle(Teal, 13f, Offset(x, rulerTop - 26f))
                    drawCircle(Color.White, 6f, Offset(x, rulerTop - 26f))
                }
            }

            Text(
                text = "Arrastra la regla y toca la marca final",
                style = MaterialTheme.typography.labelSmall,
                color = MateTheme.colors.inkSoft,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        val label = if (readingMm < 0) {
            "Sin lectura"
        } else {
            "Lectura: " + MeasureEngine.prettyLength(readingMm, unit)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Mango.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(label, style = MaterialTheme.typography.headlineSmall, color = MateTheme.colors.ink)
        }

        Spacer(Modifier.height(12.dp))

        MateButton(
            text = "Comprobar",
            onClick = {
                val answer = MeasureEngine.convert(readingMm.toDouble(), LengthUnit.MM, unit)
                onSubmit(
                    readingMm >= 0 && MeasureEngine.checkRulerReading(
                        answer, unit, payload.objectMm, payload.toleranceMm
                    )
                )
            },
            enabled = enabled && readingMm >= 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Dibuja el objeto que hay que medir. Cada uno tiene su forma propia. */
private fun DrawScope.drawMeasuredObject(
    kind: String,
    left: Float,
    top: Float,
    lengthPx: Float,
    height: Float
) {
    when (kind) {
        "lapiz" -> {
            drawRoundRect(
                Sun,
                topLeft = Offset(left, top),
                size = Size(lengthPx * 0.82f, height * 0.55f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawPath(
                com.matelab.islas.ui.art.pathOf(
                    listOf(
                        Offset(left + lengthPx * 0.82f, top),
                        Offset(left + lengthPx, top + height * 0.275f),
                        Offset(left + lengthPx * 0.82f, top + height * 0.55f)
                    )
                ),
                Mango
            )
            drawRect(Coral, Offset(left, top), Size(lengthPx * 0.10f, height * 0.55f))
        }
        "clip" -> {
            drawRoundRect(
                Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(lengthPx, height * 0.5f),
                cornerRadius = CornerRadius(height * 0.25f, height * 0.25f)
            )
            drawRoundRect(
                Teal,
                topLeft = Offset(left, top),
                size = Size(lengthPx, height * 0.5f),
                cornerRadius = CornerRadius(height * 0.25f, height * 0.25f),
                style = Stroke(width = 7f)
            )
            drawRoundRect(
                Teal,
                topLeft = Offset(left + lengthPx * 0.18f, top + height * 0.12f),
                size = Size(lengthPx * 0.7f, height * 0.26f),
                cornerRadius = CornerRadius(height * 0.13f, height * 0.13f),
                style = Stroke(width = 5f)
            )
        }
        "gusano" -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top + height * 0.3f)
                var x = left
                var up = true
                while (x < left + lengthPx) {
                    val next = (x + lengthPx / 4f).coerceAtMost(left + lengthPx)
                    quadraticBezierTo(
                        (x + next) / 2f,
                        top + if (up) -height * 0.1f else height * 0.7f,
                        next,
                        top + height * 0.3f
                    )
                    x = next
                    up = !up
                }
            }
            drawPath(path, Lime2, style = Stroke(width = height * 0.42f))
            drawCircle(Deep, height * 0.06f, Offset(left + lengthPx, top + height * 0.24f))
        }
        "cinta" -> {
            drawRoundRect(
                Coral,
                topLeft = Offset(left, top + height * 0.1f),
                size = Size(lengthPx, height * 0.34f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            var x = left
            var i = 0
            while (x < left + lengthPx) {
                if (i % 2 == 1) {
                    drawRect(
                        Sand.copy(alpha = 0.7f),
                        Offset(x, top + height * 0.1f),
                        Size((lengthPx / 10f).coerceAtMost(left + lengthPx - x), height * 0.34f)
                    )
                }
                x += lengthPx / 10f
                i++
            }
        }
        "llave" -> {
            drawRoundRect(
                Color(0xFF9AA7AD),
                topLeft = Offset(left, top + height * 0.18f),
                size = Size(lengthPx, height * 0.18f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawCircle(Color(0xFF9AA7AD), height * 0.22f, Offset(left + height * 0.1f, top + height * 0.27f))
            drawCircle(Sand, height * 0.09f, Offset(left + height * 0.1f, top + height * 0.27f))
            drawRect(
                Color(0xFF9AA7AD),
                Offset(left + lengthPx * 0.82f, top + height * 0.36f),
                Size(lengthPx * 0.06f, height * 0.16f)
            )
        }
        "cuerda" -> {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top + height * 0.3f)
                quadraticBezierTo(left + lengthPx * 0.35f, top - height * 0.15f, left + lengthPx * 0.6f, top + height * 0.35f)
                quadraticBezierTo(left + lengthPx * 0.8f, top + height * 0.75f, left + lengthPx, top + height * 0.3f)
            }
            drawPath(path, Mango, style = Stroke(width = height * 0.22f))
        }
        else -> { // tabla y otros objetos rectos
            drawRoundRect(
                Mango,
                topLeft = Offset(left, top),
                size = Size(lengthPx, height * 0.5f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                Deep.copy(alpha = 0.4f),
                topLeft = Offset(left, top),
                size = Size(lengthPx, height * 0.5f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3f)
            )
        }
    }
}

private val Lime2 = Color(0xFF7ED957)

internal fun mmDifference(a: Int, b: Int): Int = abs(a - b)
