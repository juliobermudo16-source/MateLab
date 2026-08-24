package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.SymmetryAxis

/**
 * Motor del taller de simetria: el nino completa el reflejo de un mosaico.
 *
 * Las celdas se identifican por su indice `fila * columnas + columna`.
 */
object SymmetryEngine {

    /** Indice de la celda reflejada respecto al eje. */
    fun mirrorIndex(index: Int, rows: Int, cols: Int, axis: SymmetryAxis): Int {
        require(rows > 0 && cols > 0) { "La cuadricula necesita filas y columnas" }
        require(index in 0 until rows * cols) { "Celda fuera de la cuadricula: $index" }
        val r = index / cols
        val c = index % cols
        return when (axis) {
            SymmetryAxis.VERTICAL -> r * cols + (cols - 1 - c)
            SymmetryAxis.HORIZONTAL -> (rows - 1 - r) * cols + c
        }
    }

    /** Celdas que el nino debe pintar para que el dibujo quede simetrico. */
    fun expectedCells(given: Collection<Int>, rows: Int, cols: Int, axis: SymmetryAxis): Set<Int> {
        val givenSet = given.toSet()
        return givenSet
            .map { mirrorIndex(it, rows, cols, axis) }
            .filter { it !in givenSet }
            .toSet()
    }

    /** Celdas correctas que aun faltan. */
    fun missingCells(
        given: Collection<Int>,
        painted: Collection<Int>,
        rows: Int,
        cols: Int,
        axis: SymmetryAxis
    ): Set<Int> = expectedCells(given, rows, cols, axis) - painted.toSet()

    /** Celdas pintadas que sobran (rompen la simetria). */
    fun extraCells(
        given: Collection<Int>,
        painted: Collection<Int>,
        rows: Int,
        cols: Int,
        axis: SymmetryAxis
    ): Set<Int> = painted.toSet() - expectedCells(given, rows, cols, axis) - given.toSet()

    fun isComplete(
        given: Collection<Int>,
        painted: Collection<Int>,
        rows: Int,
        cols: Int,
        axis: SymmetryAxis
    ): Boolean {
        if (given.isEmpty()) return false
        return missingCells(given, painted, rows, cols, axis).isEmpty() &&
            extraCells(given, painted, rows, cols, axis).isEmpty()
    }

    /** Porcentaje de avance, para la barra de progreso del reto. */
    fun completionPercent(
        given: Collection<Int>,
        painted: Collection<Int>,
        rows: Int,
        cols: Int,
        axis: SymmetryAxis
    ): Int {
        val expected = expectedCells(given, rows, cols, axis)
        if (expected.isEmpty()) return 100
        val hits = painted.toSet().count { it in expected }
        return (hits * 100) / expected.size
    }

    /** True si la celda pertenece a la mitad que el nino puede pintar. */
    fun isEditable(index: Int, rows: Int, cols: Int, axis: SymmetryAxis, given: Collection<Int>): Boolean {
        if (index in given) return false
        val r = index / cols
        val c = index % cols
        return when (axis) {
            SymmetryAxis.VERTICAL -> c >= (cols + 1) / 2
            SymmetryAxis.HORIZONTAL -> r >= (rows + 1) / 2
        }
    }
}
