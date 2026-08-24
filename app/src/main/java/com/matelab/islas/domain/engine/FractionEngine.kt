package com.matelab.islas.domain.engine

import kotlin.math.abs

/**
 * Fraccion con signo, siempre normalizada para que el denominador sea positivo.
 * Es el tipo base del Volcan Fraccion y de la recta numerica.
 */
data class Fraction(val numerator: Int, val denominator: Int) : Comparable<Fraction> {

    init {
        require(denominator != 0) { "El denominador no puede ser 0" }
    }

    /** Numerador con el signo ya movido al numerador. */
    val n: Int get() = if (denominator < 0) -numerator else numerator

    /** Denominador siempre positivo. */
    val d: Int get() = abs(denominator)

    fun simplified(): Fraction {
        if (n == 0) return Fraction(0, 1)
        val g = FractionEngine.gcd(abs(n), d)
        return Fraction(n / g, d / g)
    }

    fun toDouble(): Double = n.toDouble() / d.toDouble()

    operator fun plus(other: Fraction): Fraction =
        Fraction(n * other.d + other.n * d, d * other.d).simplified()

    operator fun minus(other: Fraction): Fraction =
        Fraction(n * other.d - other.n * d, d * other.d).simplified()

    operator fun times(other: Fraction): Fraction =
        Fraction(n * other.n, d * other.d).simplified()

    override fun compareTo(other: Fraction): Int =
        (n.toLong() * other.d).compareTo(other.n.toLong() * d)

    /** 3/4 y 6/8 son equivalentes aunque no sean iguales. */
    fun isEquivalentTo(other: Fraction): Boolean =
        n.toLong() * other.d == other.n.toLong() * d

    val isProper: Boolean get() = abs(n) < d

    /** Devuelve (entero, numerador, denominador) del numero mixto. */
    fun toMixed(): Triple<Int, Int, Int> {
        val whole = n / d
        val rest = abs(n % d)
        return Triple(whole, rest, d)
    }

    fun display(): String = "$n/$d"

    override fun toString(): String = display()
}

/**
 * Reglas puras de fracciones. Sin dependencias de Android: se prueba en la JVM.
 */
object FractionEngine {

    fun gcd(a: Int, b: Int): Int {
        var x = abs(a)
        var y = abs(b)
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0) 1 else x
    }

    fun lcm(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return abs(a / gcd(a, b) * b)
    }

    /** -1, 0 o 1 comparando a con b. Util para el reto de la balanza. */
    fun compare(a: Fraction, b: Fraction): Int = a.compareTo(b).coerceIn(-1, 1)

    /** Simbolo que debe arrastrar el nino al comparar dos fracciones. */
    fun comparisonSymbol(a: Fraction, b: Fraction): String = when (compare(a, b)) {
        -1 -> "<"
        1 -> ">"
        else -> "="
    }

    /**
     * Fracciones equivalentes a [f] con denominador <= [maxDenominator].
     * No incluye la propia [f] simplificada dos veces.
     */
    fun equivalents(f: Fraction, maxDenominator: Int): List<Fraction> {
        val base = f.simplified()
        if (base.d > maxDenominator) return emptyList()
        val out = mutableListOf<Fraction>()
        var k = 1
        while (base.d * k <= maxDenominator) {
            out += Fraction(base.n * k, base.d * k)
            k++
        }
        return out
    }

    /**
     * Comprueba la respuesta del reto "pintar porciones".
     * [painted] es cuantas porciones ha pintado el nino sobre [parts] totales.
     */
    fun checkPainted(painted: Int, parts: Int, targetNumerator: Int, targetDenominator: Int): Boolean {
        if (parts <= 0 || painted < 0 || painted > parts) return false
        if (targetDenominator == 0) return false
        return Fraction(painted, parts).isEquivalentTo(Fraction(targetNumerator, targetDenominator))
    }

    /**
     * Posicion (0..1) de una fraccion dentro de una recta de [wholes] unidades.
     * Se recorta para que un valor fuera de rango no rompa el dibujo.
     */
    fun positionOnLine(f: Fraction, wholes: Int): Double {
        require(wholes > 0) { "La recta necesita al menos una unidad" }
        return (f.toDouble() / wholes).coerceIn(0.0, 1.0)
    }

    /**
     * Convierte la posicion arrastrada (0..1) a la marca mas cercana de la recta.
     */
    fun snapToStep(position: Double, denominator: Int, wholes: Int): Int {
        require(denominator > 0 && wholes > 0)
        val steps = denominator * wholes
        return Math.round(position.coerceIn(0.0, 1.0) * steps).toInt()
    }

    /** Acierta si la marca elegida esta dentro de la tolerancia. */
    fun checkLine(chosenStep: Int, targetNumerator: Int, toleranceSteps: Int): Boolean =
        abs(chosenStep - targetNumerator) <= toleranceSteps

    /** Texto tipo "1 1/2" para numeros mixtos, o "3/4" si es propia. */
    fun mixedLabel(f: Fraction): String {
        val (whole, rest, den) = f.simplified().toMixed()
        return when {
            rest == 0 -> whole.toString()
            whole == 0 -> "$rest/$den"
            else -> "$whole $rest/$den"
        }
    }

    /** Suma con denominadores distintos, mostrando el comun para explicarla. */
    fun commonDenominator(a: Fraction, b: Fraction): Int = lcm(a.d, b.d)
}
