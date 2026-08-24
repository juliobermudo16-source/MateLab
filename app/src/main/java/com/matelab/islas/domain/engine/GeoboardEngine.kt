package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.GeoObjective
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/** Clavo del geoplano. Coordenadas enteras. */
data class GridPoint(val x: Int, val y: Int)

/** Resultado de medir un poligono construido por el nino. */
data class PolygonMeasure(
    val valid: Boolean,
    val area: Double,
    val perimeter: Double,
    val sides: Int,
    val problem: String? = null
)

/**
 * Motor del geoplano: mide poligonos dibujados sobre una malla de clavos.
 *
 * El area se calcula con la formula del zapato (shoelace) y el perimetro
 * sumando distancias euclideas, asi que funciona con cualquier poligono
 * simple, no solo con rectangulos.
 */
object GeoboardEngine {

    /** Area con signo. Positiva si los vertices van en sentido antihorario. */
    fun signedArea(points: List<GridPoint>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            sum += (a.x.toDouble() * b.y) - (b.x.toDouble() * a.y)
        }
        return sum / 2.0
    }

    fun area(points: List<GridPoint>): Double = abs(signedArea(points))

    fun perimeter(points: List<GridPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            total += hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble())
        }
        return total
    }

    /** True si hay vertices repetidos (el nino toco dos veces el mismo clavo). */
    fun hasDuplicates(points: List<GridPoint>): Boolean =
        points.size != points.toSet().size

    /**
     * Poligono simple: al menos 3 vertices, sin repetidos, sin lados que se
     * crucen y con area distinta de cero.
     */
    fun isSimplePolygon(points: List<GridPoint>): Boolean {
        if (points.size < 3) return false
        if (hasDuplicates(points)) return false
        if (area(points) <= 0.0) return false
        val n = points.size
        for (i in 0 until n) {
            val a1 = points[i]
            val a2 = points[(i + 1) % n]
            for (j in i + 1 until n) {
                // Lados consecutivos comparten vertice: no cuentan como cruce.
                if (j == i) continue
                if ((j + 1) % n == i || j == (i + 1) % n) continue
                val b1 = points[j]
                val b2 = points[(j + 1) % n]
                if (segmentsIntersect(a1, a2, b1, b2)) return false
            }
        }
        return true
    }

    fun measure(points: List<GridPoint>): PolygonMeasure {
        if (points.size < 3) {
            return PolygonMeasure(false, 0.0, 0.0, points.size, "Necesitas al menos 3 clavos.")
        }
        if (hasDuplicates(points)) {
            return PolygonMeasure(false, 0.0, 0.0, points.size, "Has repetido un clavo.")
        }
        if (area(points) == 0.0) {
            return PolygonMeasure(
                false, 0.0, perimeter(points), points.size,
                "Asi la figura no encierra ningun espacio."
            )
        }
        if (!isSimplePolygon(points)) {
            return PolygonMeasure(false, area(points), perimeter(points), points.size, "Las gomas se cruzan.")
        }
        return PolygonMeasure(true, area(points), perimeter(points), points.size)
    }

    /** Comprueba el objetivo del reto con la tolerancia propia de cada medida. */
    fun matchesObjective(
        points: List<GridPoint>,
        objective: GeoObjective,
        target: Double
    ): Boolean {
        val m = measure(points)
        if (!m.valid) return false
        return when (objective) {
            GeoObjective.AREA -> abs(m.area - target) < 0.001
            GeoObjective.PERIMETRO -> abs(m.perimeter - target) < 0.05
            GeoObjective.LADOS -> m.sides == target.toInt()
        }
    }

    /** Nombre de la figura segun el numero de lados. */
    fun polygonName(sides: Int): String = when (sides) {
        3 -> "triangulo"
        4 -> "cuadrilatero"
        5 -> "pentagono"
        6 -> "hexagono"
        7 -> "heptagono"
        8 -> "octogono"
        else -> "poligono de $sides lados"
    }

    /** Redondeo a 2 decimales para mostrar el perimetro sin marear. */
    fun round2(value: Double): Double = round(value * 100.0) / 100.0

    // ---------------------------------------------------------------- interno

    private fun orientation(p: GridPoint, q: GridPoint, r: GridPoint): Int {
        val v = (q.y - p.y).toLong() * (r.x - q.x) - (q.x - p.x).toLong() * (r.y - q.y)
        return when {
            v > 0 -> 1
            v < 0 -> -1
            else -> 0
        }
    }

    private fun onSegment(p: GridPoint, q: GridPoint, r: GridPoint): Boolean =
        q.x in min(p.x, r.x)..max(p.x, r.x) && q.y in min(p.y, r.y)..max(p.y, r.y)

    private fun segmentsIntersect(p1: GridPoint, q1: GridPoint, p2: GridPoint, q2: GridPoint): Boolean {
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)
        if (o1 != o2 && o3 != o4) return true
        if (o1 == 0 && onSegment(p1, p2, q1)) return true
        if (o2 == 0 && onSegment(p1, q2, q1)) return true
        if (o3 == 0 && onSegment(p2, p1, q2)) return true
        if (o4 == 0 && onSegment(p2, q1, q2)) return true
        return false
    }
}
