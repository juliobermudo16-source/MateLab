package com.matelab.islas.domain.engine

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.round

/** Tipos de angulo que se estudian entre 8 y 12 anos. */
enum class AngleKind(val label: String) {
    AGUDO("agudo"),
    RECTO("recto"),
    OBTUSO("obtuso"),
    LLANO("llano"),
    REFLEJO("reflejo"),
    COMPLETO("completo")
}

/**
 * Motor del transportador giratorio de la Bahia de las Formas.
 */
object AngleEngine {

    /** Lleva cualquier angulo al rango [0, 360). */
    fun normalize(degrees: Double): Double {
        var d = degrees % 360.0
        if (d < 0) d += 360.0
        return d
    }

    fun normalize(degrees: Int): Int {
        var d = degrees % 360
        if (d < 0) d += 360
        return d
    }

    /** Angulo del vector (dx, dy) medido en sentido antihorario desde el este. */
    fun angleOf(dx: Double, dy: Double): Double = normalize(Math.toDegrees(atan2(-dy, dx)))

    /** Diferencia mas corta entre dos angulos, siempre en [0, 180]. */
    fun shortestDelta(a: Double, b: Double): Double {
        val diff = abs(normalize(a) - normalize(b))
        return if (diff > 180.0) 360.0 - diff else diff
    }

    fun withinTolerance(actual: Double, target: Double, tolerance: Int): Boolean =
        shortestDelta(actual, target) <= tolerance

    fun classify(degrees: Double): AngleKind {
        val d = normalize(degrees)
        return when {
            d == 0.0 -> AngleKind.COMPLETO
            d < 90.0 -> AngleKind.AGUDO
            d == 90.0 -> AngleKind.RECTO
            d < 180.0 -> AngleKind.OBTUSO
            d == 180.0 -> AngleKind.LLANO
            else -> AngleKind.REFLEJO
        }
    }

    /** Imanta el giro a multiplos de [step] grados para que sea jugable. */
    fun snap(degrees: Double, step: Int = 1): Double {
        if (step <= 1) return normalize(round(degrees))
        return normalize(round(degrees / step) * step.toDouble())
    }

    /** Angulo que falta para completar un giro llano (180) o completo (360). */
    fun complementTo(degrees: Double, whole: Int): Double = normalize(whole - normalize(degrees))

    /** Suma de los angulos interiores de un poligono de [sides] lados. */
    fun interiorAngleSum(sides: Int): Int {
        require(sides >= 3) { "Un poligono necesita 3 lados o mas" }
        return (sides - 2) * 180
    }

    /** Cada angulo interior de un poligono regular. */
    fun regularInteriorAngle(sides: Int): Double =
        interiorAngleSum(sides).toDouble() / sides
}
