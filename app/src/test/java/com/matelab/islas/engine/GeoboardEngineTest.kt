package com.matelab.islas.engine

import com.matelab.islas.domain.engine.GeoboardEngine
import com.matelab.islas.domain.engine.GridPoint
import com.matelab.islas.domain.model.GeoObjective
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoboardEngineTest {

    private val cuadrado2x2 = listOf(
        GridPoint(0, 0), GridPoint(2, 0), GridPoint(2, 2), GridPoint(0, 2)
    )

    private val rectangulo3x2 = listOf(
        GridPoint(0, 0), GridPoint(3, 0), GridPoint(3, 2), GridPoint(0, 2)
    )

    @Test
    fun `area de un cuadrado de lado 2`() {
        assertEquals(4.0, GeoboardEngine.area(cuadrado2x2), 0.0001)
    }

    @Test
    fun `perimetro de un cuadrado de lado 2`() {
        assertEquals(8.0, GeoboardEngine.perimeter(cuadrado2x2), 0.0001)
    }

    @Test
    fun `area de un rectangulo 3 por 2`() {
        assertEquals(6.0, GeoboardEngine.area(rectangulo3x2), 0.0001)
    }

    @Test
    fun `un triangulo ocupa la mitad de su rectangulo`() {
        val triangulo = listOf(GridPoint(0, 0), GridPoint(2, 0), GridPoint(0, 2))
        assertEquals(2.0, GeoboardEngine.area(triangulo), 0.0001)
    }

    @Test
    fun `el orden de los vertices no cambia el area`() {
        val invertido = cuadrado2x2.reversed()
        assertEquals(GeoboardEngine.area(cuadrado2x2), GeoboardEngine.area(invertido), 0.0001)
    }

    @Test
    fun `menos de tres clavos no forma poligono`() {
        val medida = GeoboardEngine.measure(listOf(GridPoint(0, 0), GridPoint(1, 1)))
        assertFalse(medida.valid)
        assertNotNull(medida.problem)
    }

    @Test
    fun `clavos repetidos se detectan`() {
        val repetido = listOf(GridPoint(0, 0), GridPoint(2, 0), GridPoint(0, 0))
        assertTrue(GeoboardEngine.hasDuplicates(repetido))
        assertFalse(GeoboardEngine.measure(repetido).valid)
    }

    @Test
    fun `tres clavos en linea recta no encierran area`() {
        val alineados = listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(2, 0))
        val medida = GeoboardEngine.measure(alineados)
        assertFalse(medida.valid)
        assertEquals(0.0, medida.area, 0.0001)
    }

    @Test
    fun `un poligono cruzado no es valido`() {
        val cruzado = listOf(
            GridPoint(0, 0), GridPoint(2, 2), GridPoint(2, 0), GridPoint(0, 2)
        )
        assertFalse(GeoboardEngine.isSimplePolygon(cruzado))
    }

    @Test
    fun `objetivo de area se cumple`() {
        assertTrue(GeoboardEngine.matchesObjective(rectangulo3x2, GeoObjective.AREA, 6.0))
        assertFalse(GeoboardEngine.matchesObjective(rectangulo3x2, GeoObjective.AREA, 5.0))
    }

    @Test
    fun `objetivo de perimetro se cumple`() {
        assertTrue(GeoboardEngine.matchesObjective(rectangulo3x2, GeoObjective.PERIMETRO, 10.0))
    }

    @Test
    fun `objetivo de numero de lados se cumple`() {
        val pentagono = listOf(
            GridPoint(0, 0), GridPoint(2, 0), GridPoint(3, 2), GridPoint(1, 3), GridPoint(0, 2)
        )
        assertTrue(GeoboardEngine.matchesObjective(pentagono, GeoObjective.LADOS, 5.0))
        assertFalse(GeoboardEngine.matchesObjective(pentagono, GeoObjective.LADOS, 4.0))
    }

    @Test
    fun `un poligono invalido nunca cumple el objetivo`() {
        val alineados = listOf(GridPoint(0, 0), GridPoint(1, 0), GridPoint(2, 0))
        assertFalse(GeoboardEngine.matchesObjective(alineados, GeoObjective.AREA, 0.0))
    }

    @Test
    fun `nombres de poligonos`() {
        assertEquals("triangulo", GeoboardEngine.polygonName(3))
        assertEquals("hexagono", GeoboardEngine.polygonName(6))
        assertEquals("poligono de 11 lados", GeoboardEngine.polygonName(11))
    }

    @Test
    fun `redondeo a dos decimales`() {
        assertEquals(2.83, GeoboardEngine.round2(2.8284), 0.0001)
    }

    @Test
    fun `lista vacia no rompe el motor`() {
        assertEquals(0.0, GeoboardEngine.area(emptyList()), 0.0001)
        assertEquals(0.0, GeoboardEngine.perimeter(emptyList()), 0.0001)
        assertFalse(GeoboardEngine.measure(emptyList()).valid)
    }
}
