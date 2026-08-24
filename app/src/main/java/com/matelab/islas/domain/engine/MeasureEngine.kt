package com.matelab.islas.domain.engine

import kotlin.math.abs
import kotlin.math.round

/** Unidades de longitud expresadas en milimetros. */
enum class LengthUnit(val symbol: String, val inMillimeters: Double) {
    MM("mm", 1.0),
    CM("cm", 10.0),
    DM("dm", 100.0),
    M("m", 1_000.0),
    KM("km", 1_000_000.0)
}

/** Unidades de masa expresadas en gramos. */
enum class MassUnit(val symbol: String, val inGrams: Double) {
    G("g", 1.0),
    KG("kg", 1_000.0),
    T("t", 1_000_000.0)
}

/** Unidades de capacidad expresadas en mililitros. */
enum class CapacityUnit(val symbol: String, val inMilliliters: Double) {
    ML("ml", 1.0),
    L("L", 1_000.0)
}

/**
 * Motor de Puerto Medida: conversiones, lectura de regla y balanza.
 */
object MeasureEngine {

    fun convert(value: Double, from: LengthUnit, to: LengthUnit): Double =
        value * from.inMillimeters / to.inMillimeters

    fun convert(value: Double, from: MassUnit, to: MassUnit): Double =
        value * from.inGrams / to.inGrams

    fun convert(value: Double, from: CapacityUnit, to: CapacityUnit): Double =
        value * from.inMilliliters / to.inMilliliters

    /**
     * Cuantos peldanos hay que subir o bajar en la escalera de unidades.
     * Positivo = hacia unidades mas pequenas (multiplicar por 10).
     */
    fun ladderSteps(from: LengthUnit, to: LengthUnit): Int {
        val order = listOf(LengthUnit.KM, LengthUnit.M, LengthUnit.DM, LengthUnit.CM, LengthUnit.MM)
        // La escalera didactica salta de km a m directamente (x1000 = 3 peldanos).
        val factors = mapOf(
            LengthUnit.KM to 6, LengthUnit.M to 3, LengthUnit.DM to 2,
            LengthUnit.CM to 1, LengthUnit.MM to 0
        )
        require(from in order && to in order)
        return factors.getValue(from) - factors.getValue(to)
    }

    /**
     * Comprueba la lectura de la regla. El nino responde en [unit];
     * la tolerancia siempre se expresa en milimetros.
     */
    fun checkRulerReading(
        answer: Double,
        unit: LengthUnit,
        objectMm: Int,
        toleranceMm: Int
    ): Boolean {
        val answerMm = convert(answer, unit, LengthUnit.MM)
        return abs(answerMm - objectMm) <= toleranceMm
    }

    /** Etiqueta amable: 45 mm -> "4,5 cm". */
    fun prettyLength(millimeters: Int, unit: LengthUnit): String {
        val value = convert(millimeters.toDouble(), LengthUnit.MM, unit)
        val rounded = round(value * 10.0) / 10.0
        val text = if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString().replace('.', ',')
        }
        return "$text ${unit.symbol}"
    }

    // ------------------------------------------------------------- BALANZA

    /** Inclinacion de la balanza: -1 baja la izquierda, 1 baja la derecha. */
    fun tilt(leftGrams: Int, rightGrams: Int): Int = when {
        leftGrams > rightGrams -> -1
        rightGrams > leftGrams -> 1
        else -> 0
    }

    fun isBalanced(leftGrams: Int, rightWeights: List<Int>): Boolean =
        leftGrams > 0 && rightWeights.sum() == leftGrams

    /** Cuanto falta (positivo) o sobra (negativo) en el plato derecho. */
    fun difference(leftGrams: Int, rightWeights: List<Int>): Int =
        leftGrams - rightWeights.sum()

    /**
     * Pista real: la pesa mas grande que aun cabe sin pasarse.
     * Devuelve null si ya no hay ninguna util.
     */
    fun suggestWeight(leftGrams: Int, rightWeights: List<Int>, available: List<Int>): Int? {
        val missing = difference(leftGrams, rightWeights)
        if (missing <= 0) return null
        return available.filter { it <= missing }.maxOrNull()
    }

    /**
     * Solucion minima en numero de pesas (algoritmo voraz sobre pesas
     * multiplos entre si, que es el caso de 1/2/5/10/... del juego).
     */
    fun greedySolution(targetGrams: Int, available: List<Int>): List<Int> {
        if (targetGrams <= 0) return emptyList()
        val sorted = available.filter { it > 0 }.distinct().sortedDescending()
        var left = targetGrams
        val out = mutableListOf<Int>()
        for (w in sorted) {
            while (left >= w) {
                out += w
                left -= w
            }
        }
        return if (left == 0) out else emptyList()
    }
}
