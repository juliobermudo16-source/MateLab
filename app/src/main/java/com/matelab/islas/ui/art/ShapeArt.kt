package com.matelab.islas.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.model.ShapeSpec
import com.matelab.islas.ui.theme.Deep

/**
 * Dibuja las figuras geometricas del clasificador y de los patrones.
 * Cada figura se construye con su trazado real, no con un icono generico.
 */
@Composable
fun GeoShape(
    spec: ShapeSpec,
    color: Color,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
    outline: Boolean = true
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            rotate(spec.rotation.toFloat()) {
                drawShapeSpec(spec, this.size.width, color, outline)
            }
        }
    }
}

fun DrawScope.drawShapeSpec(spec: ShapeSpec, side: Float, color: Color, outline: Boolean = true) {
    val center = Offset(side / 2f, side / 2f)
    val r = side * 0.38f
    val strokeW = side * 0.05f

    if (spec.curved) {
        when {
            spec.name.startsWith("semi") -> {
                drawArc(
                    color, 180f, 180f, true,
                    topLeft = Offset(center.x - r, center.y - r * 0.6f),
                    size = Size(r * 2, r * 2)
                )
                if (outline) {
                    drawArc(
                        Deep.copy(alpha = 0.6f), 180f, 180f, true,
                        topLeft = Offset(center.x - r, center.y - r * 0.6f),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = strokeW)
                    )
                }
            }
            spec.name.startsWith("ovalo") -> {
                drawOval(color, Offset(center.x - r, center.y - r * 0.65f), Size(r * 2, r * 1.3f))
                if (outline) {
                    drawOval(
                        Deep.copy(alpha = 0.6f),
                        Offset(center.x - r, center.y - r * 0.65f),
                        Size(r * 2, r * 1.3f),
                        style = Stroke(width = strokeW)
                    )
                }
            }
            else -> {
                drawCircle(color, r, center)
                if (outline) drawCircle(Deep.copy(alpha = 0.6f), r, center, style = Stroke(width = strokeW))
            }
        }
        return
    }

    val path = when {
        spec.sides == 3 && spec.rightAngles > 0 -> pathOf(
            listOf(
                Offset(center.x - r, center.y + r * 0.8f),
                Offset(center.x - r, center.y - r * 0.9f),
                Offset(center.x + r, center.y + r * 0.8f)
            )
        )
        spec.sides == 3 && !spec.allSidesEqual -> pathOf(
            listOf(
                Offset(center.x - r, center.y + r * 0.7f),
                Offset(center.x - r * 0.15f, center.y - r),
                Offset(center.x + r, center.y + r * 0.4f)
            )
        )
        spec.sides == 3 -> polygonPath(center, r, 3)
        spec.sides == 4 && spec.allSidesEqual && spec.rightAngles == 4 -> pathOf(
            listOf(
                Offset(center.x - r * 0.8f, center.y - r * 0.8f),
                Offset(center.x + r * 0.8f, center.y - r * 0.8f),
                Offset(center.x + r * 0.8f, center.y + r * 0.8f),
                Offset(center.x - r * 0.8f, center.y + r * 0.8f)
            )
        )
        spec.sides == 4 && spec.rightAngles == 4 -> pathOf(
            listOf(
                Offset(center.x - r, center.y - r * 0.55f),
                Offset(center.x + r, center.y - r * 0.55f),
                Offset(center.x + r, center.y + r * 0.55f),
                Offset(center.x - r, center.y + r * 0.55f)
            )
        )
        spec.sides == 4 && spec.allSidesEqual -> diamondPath(center, r)
        spec.sides == 4 && spec.rightAngles == 2 -> pathOf(
            listOf(
                Offset(center.x - r * 0.55f, center.y - r * 0.65f),
                Offset(center.x + r * 0.55f, center.y - r * 0.65f),
                Offset(center.x + r, center.y + r * 0.65f),
                Offset(center.x - r, center.y + r * 0.65f)
            )
        )
        spec.sides == 4 -> pathOf(
            listOf(
                Offset(center.x - r * 0.55f, center.y - r * 0.6f),
                Offset(center.x + r, center.y - r * 0.6f),
                Offset(center.x + r * 0.55f, center.y + r * 0.6f),
                Offset(center.x - r, center.y + r * 0.6f)
            )
        )
        spec.sides >= 5 -> polygonPath(center, r, spec.sides)
        else -> polygonPath(center, r, 4)
    }

    drawPath(path, color)
    if (outline) drawPath(path, Deep.copy(alpha = 0.6f), style = Stroke(width = strokeW))
}

/** Figura simple por nombre, usada en los patrones. */
@Composable
fun TokenShape(
    shape: String,
    color: Color,
    size: Dp = 44.dp,
    rotationDegrees: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            rotate(rotationDegrees.toFloat()) {
                drawTokenShape(shape, this.size.width, color)
            }
        }
    }
}

fun DrawScope.drawTokenShape(shape: String, side: Float, color: Color) {
    val center = Offset(side / 2f, side / 2f)
    val r = side * 0.36f
    val stroke = side * 0.055f
    val path: Path? = when (shape) {
        "triangulo" -> polygonPath(center, r, 3)
        "cuadrado" -> polygonPath(center, r, 4, 45f)
        "rombo" -> diamondPath(center, r)
        "pentagono" -> polygonPath(center, r, 5)
        "hexagono" -> polygonPath(center, r, 6)
        "estrella" -> starPath(center, r, r * 0.45f)
        else -> null
    }
    if (path == null) {
        drawCircle(color, r, center)
        drawCircle(Deep.copy(alpha = 0.55f), r, center, style = Stroke(width = stroke))
    } else {
        drawPath(path, color)
        drawPath(path, Deep.copy(alpha = 0.55f), style = Stroke(width = stroke))
    }
}
