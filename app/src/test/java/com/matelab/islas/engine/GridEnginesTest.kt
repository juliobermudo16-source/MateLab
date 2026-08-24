package com.matelab.islas.engine

import com.matelab.islas.domain.engine.PatternEngine
import com.matelab.islas.domain.engine.PlaceValueEngine
import com.matelab.islas.domain.engine.ShapeSortEngine
import com.matelab.islas.domain.engine.SymmetryEngine
import com.matelab.islas.domain.model.BucketSpec
import com.matelab.islas.domain.model.ShapeCriterion
import com.matelab.islas.domain.model.ShapeSpec
import com.matelab.islas.domain.model.SymmetryAxis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GridEnginesTest {

    // ------------------------------------------------------------ simetria

    @Test
    fun `reflejo de una celda con eje vertical`() {
        assertEquals(5, SymmetryEngine.mirrorIndex(0, rows = 4, cols = 6, axis = SymmetryAxis.VERTICAL))
        assertEquals(6, SymmetryEngine.mirrorIndex(11, rows = 4, cols = 6, axis = SymmetryAxis.VERTICAL))
    }

    @Test
    fun `reflejo de una celda con eje horizontal`() {
        assertEquals(18, SymmetryEngine.mirrorIndex(0, rows = 4, cols = 6, axis = SymmetryAxis.HORIZONTAL))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `una celda fuera de la cuadricula lanza excepcion`() {
        SymmetryEngine.mirrorIndex(99, rows = 4, cols = 6, axis = SymmetryAxis.VERTICAL)
    }

    @Test
    fun `celdas esperadas y mosaico completo`() {
        val given = listOf(0, 6)
        val esperadas = SymmetryEngine.expectedCells(given, rows = 2, cols = 4, axis = SymmetryAxis.VERTICAL)
        assertEquals(setOf(3, 5), esperadas)
        assertTrue(SymmetryEngine.isComplete(given, listOf(3, 5), 2, 4, SymmetryAxis.VERTICAL))
        assertFalse(SymmetryEngine.isComplete(given, listOf(3), 2, 4, SymmetryAxis.VERTICAL))
    }

    @Test
    fun `pintar de mas rompe la simetria`() {
        val given = listOf(0)
        assertFalse(SymmetryEngine.isComplete(given, listOf(3, 2), 2, 4, SymmetryAxis.VERTICAL))
        assertEquals(setOf(2), SymmetryEngine.extraCells(given, listOf(3, 2), 2, 4, SymmetryAxis.VERTICAL))
    }

    @Test
    fun `porcentaje de avance del mosaico`() {
        val given = listOf(0, 4)
        assertEquals(50, SymmetryEngine.completionPercent(given, listOf(3), 2, 4, SymmetryAxis.VERTICAL))
        assertEquals(100, SymmetryEngine.completionPercent(given, listOf(3, 7), 2, 4, SymmetryAxis.VERTICAL))
    }

    @Test
    fun `solo se puede pintar la mitad libre`() {
        assertFalse(SymmetryEngine.isEditable(0, 4, 6, SymmetryAxis.VERTICAL, emptyList()))
        assertTrue(SymmetryEngine.isEditable(5, 4, 6, SymmetryAxis.VERTICAL, emptyList()))
        assertFalse(SymmetryEngine.isEditable(5, 4, 6, SymmetryAxis.VERTICAL, listOf(5)))
    }

    @Test
    fun `sin celdas de partida el mosaico no puede estar completo`() {
        assertFalse(SymmetryEngine.isComplete(emptyList(), emptyList(), 4, 4, SymmetryAxis.VERTICAL))
    }

    // ------------------------------------------------------ valor posicional

    @Test
    fun `valor de un conjunto de bloques`() {
        assertEquals(152, PlaceValueEngine.valueOf(mapOf(100 to 1, 10 to 5, 1 to 2)))
        assertEquals(0, PlaceValueEngine.valueOf(emptyMap()))
    }

    @Test
    fun `los bloques negativos se ignoran`() {
        assertEquals(100, PlaceValueEngine.valueOf(mapOf(100 to 1, 10 to -3)))
    }

    @Test
    fun `descomposicion canonica`() {
        val descomposicion = PlaceValueEngine.decompose(1230)
        assertEquals(1, descomposicion[1000])
        assertEquals(2, descomposicion[100])
        assertEquals(3, descomposicion[10])
        assertEquals(0, descomposicion[1])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `no se descomponen numeros negativos`() {
        PlaceValueEngine.decompose(-5)
    }

    @Test
    fun `texto de la descomposicion`() {
        assertEquals("4 centenas + 7 unidades", PlaceValueEngine.decompositionLabel(407))
        assertEquals("0 unidades", PlaceValueEngine.decompositionLabel(0))
    }

    @Test
    fun `comprobar el numero construido`() {
        assertTrue(PlaceValueEngine.isCorrect(mapOf(100 to 4, 1 to 7), 407))
        assertFalse(PlaceValueEngine.isCorrect(mapOf(100 to 4, 10 to 1, 1 to 7), 407))
        assertEquals(-10, PlaceValueEngine.remaining(mapOf(100 to 4, 10 to 1, 1 to 7), 407))
    }

    @Test
    fun `sugerencia de la pieza mas grande que cabe`() {
        assertEquals(100, PlaceValueEngine.suggestPiece(mapOf(1000 to 1), 1230))
        assertNull(PlaceValueEngine.suggestPiece(mapOf(1000 to 2), 1230))
    }

    @Test
    fun `digito y valor posicional`() {
        assertEquals(7, PlaceValueEngine.digitAt(4703, 2))
        assertEquals(700, PlaceValueEngine.positionalValue(4703, 2))
        assertEquals(0, PlaceValueEngine.digitAt(4703, 1))
    }

    // ------------------------------------------------------------- patrones

    @Test
    fun `paso de una progresion aritmetica`() {
        assertEquals(4, PatternEngine.arithmeticStep(listOf(3, 7, 11, 15)))
        assertNull(PatternEngine.arithmeticStep(listOf(3, 7, 12)))
        assertNull(PatternEngine.arithmeticStep(listOf(3, 7)))
    }

    @Test
    fun `razon de una progresion geometrica`() {
        assertEquals(2, PatternEngine.geometricRatio(listOf(2, 4, 8, 16)))
        assertNull(PatternEngine.geometricRatio(listOf(2, 4, 9)))
        assertNull(PatternEngine.geometricRatio(listOf(0, 0, 0)))
    }

    @Test
    fun `siguiente valor de la serie`() {
        assertEquals(19, PatternEngine.nextValue(listOf(3, 7, 11, 15)))
        assertEquals(32, PatternEngine.nextValue(listOf(2, 4, 8, 16)))
        assertNull(PatternEngine.nextValue(listOf(1, 2, 4, 7)))
    }

    @Test
    fun `rellenar el hueco de una serie que suma`() {
        assertEquals(15, PatternEngine.fillHole(listOf(3, 7, 11, null, 19)))
        assertEquals(28, PatternEngine.fillHole(listOf(40, 34, null, 22, 16)))
    }

    @Test
    fun `rellenar el hueco de una serie que multiplica`() {
        assertEquals(16, PatternEngine.fillHole(listOf(2, 4, 8, null, 32)))
        assertEquals(27, PatternEngine.fillHole(listOf(1, 3, 9, null, 81)))
    }

    @Test
    fun `una serie sin hueco no devuelve nada`() {
        assertNull(PatternEngine.fillHole(listOf(1, 2, 3)))
    }

    @Test
    fun `unidad que se repite en un patron de figuras`() {
        assertEquals(
            listOf("a", "b", "c"),
            PatternEngine.repeatingUnit(listOf("a", "b", "c", "a", "b", "c"))
        )
        assertEquals(listOf("a"), PatternEngine.repeatingUnit(listOf("a", "a", "a")))
    }

    @Test
    fun `predecir la figura que falta`() {
        val tokens = listOf("a", "b", "c", "a", "b", "c", "a", null)
        assertEquals("b", PatternEngine.predictHole(tokens))
    }

    @Test
    fun `una secuencia demasiado corta no se adivina`() {
        assertNull(PatternEngine.predictHole(listOf("a", "b", null)))
    }

    @Test
    fun `validar la opcion elegida`() {
        assertTrue(PatternEngine.isCorrect(2, 2))
        assertFalse(PatternEngine.isCorrect(-1, -1))
        assertFalse(PatternEngine.isCorrect(1, 2))
    }

    // --------------------------------------------------------- clasificador

    private val triangulo = ShapeSpec("t", "triangulo equilatero", 3, true, 0)
    private val cuadrado = ShapeSpec("c", "cuadrado", 4, true, 4)
    private val circulo = ShapeSpec("o", "circulo", 0, false, 0, curved = true)
    private val rectangulo = ShapeSpec("r", "rectangulo", 4, false, 4)

    private val cestasPorLados = listOf(
        BucketSpec("b3", "3 lados", ShapeCriterion.NUM_LADOS, 3),
        BucketSpec("b4", "4 lados", ShapeCriterion.NUM_LADOS, 4)
    )

    @Test
    fun `cada figura cae en su cesta por numero de lados`() {
        assertEquals("b3", ShapeSortEngine.bucketFor(triangulo, cestasPorLados))
        assertEquals("b4", ShapeSortEngine.bucketFor(cuadrado, cestasPorLados))
        assertNull(ShapeSortEngine.bucketFor(circulo, cestasPorLados))
    }

    @Test
    fun `una figura curva no tiene lados contables`() {
        val cestas = listOf(BucketSpec("bc", "curva", ShapeCriterion.ES_CURVA))
        assertTrue(ShapeSortEngine.matches(circulo, cestas.first()))
        assertFalse(ShapeSortEngine.matches(cuadrado, cestas.first()))
    }

    @Test
    fun `angulo recto y lados iguales`() {
        val recto = BucketSpec("br", "recto", ShapeCriterion.TIENE_ANGULO_RECTO)
        val iguales = BucketSpec("bi", "iguales", ShapeCriterion.LADOS_IGUALES)
        assertTrue(ShapeSortEngine.matches(rectangulo, recto))
        assertFalse(ShapeSortEngine.matches(triangulo, recto))
        assertTrue(ShapeSortEngine.matches(triangulo, iguales))
        assertFalse(ShapeSortEngine.matches(rectangulo, iguales))
    }

    @Test
    fun `el reto solo se resuelve con todas las figuras bien colocadas`() {
        val figuras = listOf(triangulo, cuadrado)
        val correcto = mapOf("t" to "b3", "c" to "b4")
        val incompleto = mapOf("t" to "b3")
        val erroneo = mapOf("t" to "b4", "c" to "b3")
        assertTrue(ShapeSortEngine.isSolved(correcto, figuras, cestasPorLados))
        assertFalse(ShapeSortEngine.isSolved(incompleto, figuras, cestasPorLados))
        assertFalse(ShapeSortEngine.isSolved(erroneo, figuras, cestasPorLados))
        assertEquals(0, ShapeSortEngine.countCorrect(erroneo, figuras, cestasPorLados))
    }

    @Test
    fun `un reto sin solucion se detecta`() {
        assertTrue(ShapeSortEngine.isChallengeWellFormed(listOf(triangulo, cuadrado), cestasPorLados))
        assertFalse(ShapeSortEngine.isChallengeWellFormed(listOf(triangulo, circulo), cestasPorLados))
        assertFalse(ShapeSortEngine.isChallengeWellFormed(emptyList(), cestasPorLados))
    }
}
