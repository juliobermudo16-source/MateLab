package com.matelab.islas.domain.engine

/**
 * Motor de patrones: detecta la regla de una secuencia y predice el hueco.
 * Se usa tanto con numeros como con figuras de colores.
 */
object PatternEngine {

    /** Paso constante de una progresion aritmetica, o null si no lo es. */
    fun arithmeticStep(values: List<Int>): Int? {
        if (values.size < 3) return null
        val step = values[1] - values[0]
        for (i in 1 until values.size) {
            if (values[i] - values[i - 1] != step) return null
        }
        return step
    }

    /** Razon de una progresion geometrica de enteros, o null. */
    fun geometricRatio(values: List<Int>): Int? {
        if (values.size < 3) return null
        if (values.any { it == 0 }) return null
        val ratio = values[1] / values[0]
        if (ratio == 0) return null
        for (i in 1 until values.size) {
            if (values[i - 1] * ratio != values[i]) return null
        }
        return ratio
    }

    /** Siguiente numero de la secuencia, si la regla es reconocible. */
    fun nextValue(values: List<Int>): Int? {
        arithmeticStep(values)?.let { return values.last() + it }
        geometricRatio(values)?.let { return values.last() * it }
        return null
    }

    /**
     * Rellena el hueco de una secuencia numerica con un valor ausente.
     * [values] usa null para marcar la posicion desconocida.
     */
    fun fillHole(values: List<Int?>): Int? {
        val holeIndex = values.indexOfFirst { it == null }
        if (holeIndex < 0) return null
        val known = values.filterNotNull()
        if (known.size < 2) return null

        // Progresion aritmetica deducida de los tramos completos conocidos.
        val diffs = mutableListOf<Int>()
        for (i in 1 until values.size) {
            val a = values[i - 1]
            val b = values[i]
            if (a != null && b != null) diffs += (b - a)
        }
        if (diffs.isNotEmpty() && diffs.distinct().size == 1) {
            val step = diffs.first()
            val anchorIndex = values.indexOfFirst { it != null }
            val anchor = values[anchorIndex]!!
            return anchor + step * (holeIndex - anchorIndex)
        }

        val ratios = mutableListOf<Int>()
        for (i in 1 until values.size) {
            val a = values[i - 1]
            val b = values[i]
            if (a != null && b != null && a != 0 && b % a == 0) ratios += (b / a)
        }
        if (ratios.isNotEmpty() && ratios.distinct().size == 1) {
            val ratio = ratios.first()
            val anchorIndex = values.indexOfFirst { it != null }
            var value = values[anchorIndex]!!
            var i = anchorIndex
            while (i < holeIndex) {
                value *= ratio
                i++
            }
            while (i > holeIndex) {
                value /= ratio
                i--
            }
            return value
        }
        return null
    }

    /**
     * Unidad que se repite en un patron de figuras (AB, ABC, AABB...).
     * Devuelve la lista mas corta que genera toda la secuencia.
     */
    fun repeatingUnit(tokens: List<String>): List<String> {
        if (tokens.isEmpty()) return emptyList()
        for (size in 1..tokens.size) {
            val unit = tokens.take(size)
            if (tokens.indices.all { tokens[it] == unit[it % size] }) return unit
        }
        return tokens
    }

    /**
     * Deduce que figura va en el hueco (marcado con null) de un patron
     * ciclico. Exige que la unidad se repita al menos dos veces para no
     * "adivinar" con secuencias demasiado cortas.
     */
    fun predictHole(tokens: List<String?>): String? {
        val holeIndex = tokens.indexOfFirst { it == null }
        if (holeIndex < 0 || tokens.size < 4) return null

        for (size in 1..tokens.size / 2) {
            val unit = arrayOfNulls<String>(size)
            var consistent = true
            for (i in tokens.indices) {
                val token = tokens[i] ?: continue
                val slot = i % size
                if (unit[slot] == null) {
                    unit[slot] = token
                } else if (unit[slot] != token) {
                    consistent = false
                    break
                }
            }
            if (consistent && unit.all { it != null }) {
                return unit[holeIndex % size]
            }
        }
        return null
    }

    fun isCorrect(chosenIndex: Int, answerIndex: Int): Boolean =
        chosenIndex >= 0 && chosenIndex == answerIndex
}
