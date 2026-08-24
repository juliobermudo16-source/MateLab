package com.matelab.islas.domain.engine

/**
 * Motor de los bloques de base diez de la Cueva de los Numeros.
 *
 * El nino arrastra unidades, barras de 10, placas de 100 y cubos de 1000
 * hasta formar el numero pedido.
 */
object PlaceValueEngine {

    val PIECES = listOf(1, 10, 100, 1000)

    fun pieceName(piece: Int): String = when (piece) {
        1 -> "unidades"
        10 -> "decenas"
        100 -> "centenas"
        1000 -> "millares"
        else -> "piezas de $piece"
    }

    /** Valor total de un puñado de piezas. */
    fun valueOf(counts: Map<Int, Int>): Int =
        counts.entries.sumOf { (piece, count) -> piece * count.coerceAtLeast(0) }

    /** Descomposicion canonica de un numero en piezas de base diez. */
    fun decompose(number: Int, pieces: List<Int> = PIECES): Map<Int, Int> {
        require(number >= 0) { "No hay bloques negativos" }
        var left = number
        val out = linkedMapOf<Int, Int>()
        for (p in pieces.sortedDescending()) {
            out[p] = left / p
            left %= p
        }
        return out
    }

    /** Texto tipo "2 millares + 3 centenas + 5 unidades". */
    fun decompositionLabel(number: Int): String {
        val parts = decompose(number).entries
            .filter { it.value > 0 }
            .map { "${it.value} ${pieceName(it.key)}" }
        return if (parts.isEmpty()) "0 unidades" else parts.joinToString(" + ")
    }

    fun isCorrect(counts: Map<Int, Int>, target: Int): Boolean = valueOf(counts) == target

    /** Cuanto falta (positivo) o sobra (negativo) respecto al objetivo. */
    fun remaining(counts: Map<Int, Int>, target: Int): Int = target - valueOf(counts)

    /** Pista real: la pieza mas grande que todavia cabe. */
    fun suggestPiece(counts: Map<Int, Int>, target: Int, pieces: List<Int> = PIECES): Int? {
        val missing = remaining(counts, target)
        if (missing <= 0) return null
        return pieces.filter { it <= missing }.maxOrNull()
    }

    /** Digito de un numero en la posicion dada (0 = unidades). */
    fun digitAt(number: Int, position: Int): Int {
        require(position >= 0)
        var p = 1
        repeat(position) { p *= 10 }
        return (number / p) % 10
    }

    /** Valor posicional real de un digito: el 7 de 4703 vale 700. */
    fun positionalValue(number: Int, position: Int): Int {
        var p = 1
        repeat(position) { p *= 10 }
        return digitAt(number, position) * p
    }
}
