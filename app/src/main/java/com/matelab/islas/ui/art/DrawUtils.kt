package com.matelab.islas.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utilidades de dibujo compartidas por todas las ilustraciones.
 * Se generan trazados vectoriales, nunca imagenes de mapa de bits.
 */

fun polygonPath(
    center: Offset,
    radius: Float,
    sides: Int,
    rotationDegrees: Float = 0f
): Path {
    val path = Path()
    if (sides < 3) return path
    val step = (2.0 * Math.PI / sides).toFloat()
    val start = Math.toRadians(rotationDegrees.toDouble() - 90.0).toFloat()
    for (i in 0 until sides) {
        val a = start + step * i
        val x = center.x + radius * cos(a)
        val y = center.y + radius * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

fun starPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    points: Int = 5,
    rotationDegrees: Float = 0f
): Path {
    val path = Path()
    val step = (Math.PI / points).toFloat()
    val start = Math.toRadians(rotationDegrees.toDouble() - 90.0).toFloat()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val a = start + step * i
        val x = center.x + r * cos(a)
        val y = center.y + r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/** Trazado de una figura cerrada a partir de puntos sueltos. */
fun pathOf(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    points.drop(1).forEach { path.lineTo(it.x, it.y) }
    path.close()
    return path
}

/** Silueta ondulada, util para olas, colinas y lava. */
fun wavePath(
    rect: Rect,
    waves: Int,
    amplitude: Float,
    phase: Float = 0f
): Path {
    val path = Path()
    path.moveTo(rect.left, rect.bottom)
    path.lineTo(rect.left, rect.top + amplitude)
    val segment = rect.width / (waves * 2f)
    var x = rect.left
    var up = true
    var i = 0
    while (x < rect.right) {
        val nextX = (x + segment * 2).coerceAtMost(rect.right)
        val controlX = x + segment
        val controlY = rect.top + if (up) -amplitude + phase else amplitude * 2 + phase
        path.quadraticBezierTo(controlX, controlY, nextX, rect.top + amplitude)
        x = nextX
        up = !up
        i++
        if (i > waves * 4) break
    }
    path.lineTo(rect.right, rect.bottom)
    path.close()
    return path
}

/** Rombo (cuadrado girado 45 grados). */
fun diamondPath(center: Offset, radius: Float): Path = polygonPath(center, radius, 4, 0f)

/** Punto sobre una circunferencia. */
fun onCircle(center: Offset, radius: Float, degrees: Float): Offset {
    val a = Math.toRadians(degrees.toDouble() - 90.0)
    return Offset(
        center.x + radius * cos(a).toFloat(),
        center.y + radius * sin(a).toFloat()
    )
}

/** Generador determinista: la misma semilla dibuja siempre lo mismo. */
class ArtRandom(seed: Int) {
    private var state = (seed * 2654435761L.toInt()) or 1

    fun next(): Float {
        state = state xor (state shl 13)
        state = state xor (state ushr 17)
        state = state xor (state shl 5)
        return ((state ushr 8) and 0xFFFFFF) / 16777215f
    }

    fun between(min: Float, max: Float): Float = min + next() * (max - min)

    fun intBetween(min: Int, max: Int): Int =
        min + (next() * (max - min + 1)).toInt().coerceAtMost(max - min)
}
