package com.matelab.islas.engine

import com.matelab.islas.domain.engine.CapacityUnit
import com.matelab.islas.domain.engine.LengthUnit
import com.matelab.islas.domain.engine.MassUnit
import com.matelab.islas.domain.engine.MeasureEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasureEngineTest {

    @Test
    fun `de centimetros a milimetros`() {
        assertEquals(70.0, MeasureEngine.convert(7.0, LengthUnit.CM, LengthUnit.MM), 0.0001)
    }

    @Test
    fun `de metros a centimetros`() {
        assertEquals(300.0, MeasureEngine.convert(3.0, LengthUnit.M, LengthUnit.CM), 0.0001)
    }

    @Test
    fun `de kilometros a metros`() {
        assertEquals(2500.0, MeasureEngine.convert(2.5, LengthUnit.KM, LengthUnit.M), 0.0001)
    }

    @Test
    fun `de kilos a gramos y vuelta`() {
        assertEquals(1350.0, MeasureEngine.convert(1.35, MassUnit.KG, MassUnit.G), 0.0001)
        assertEquals(1.35, MeasureEngine.convert(1350.0, MassUnit.G, MassUnit.KG), 0.0001)
    }

    @Test
    fun `de litros a mililitros`() {
        assertEquals(1500.0, MeasureEngine.convert(1.5, CapacityUnit.L, CapacityUnit.ML), 0.0001)
    }

    @Test
    fun `peldanos de la escalera de unidades`() {
        assertEquals(1, MeasureEngine.ladderSteps(LengthUnit.CM, LengthUnit.MM))
        assertEquals(3, MeasureEngine.ladderSteps(LengthUnit.M, LengthUnit.MM))
        assertEquals(-3, MeasureEngine.ladderSteps(LengthUnit.MM, LengthUnit.M))
        assertEquals(0, MeasureEngine.ladderSteps(LengthUnit.CM, LengthUnit.CM))
    }

    @Test
    fun `lectura de regla correcta dentro de la tolerancia`() {
        assertTrue(MeasureEngine.checkRulerReading(12.0, LengthUnit.CM, objectMm = 120, toleranceMm = 2))
        assertTrue(MeasureEngine.checkRulerReading(11.9, LengthUnit.CM, objectMm = 120, toleranceMm = 2))
        assertFalse(MeasureEngine.checkRulerReading(11.0, LengthUnit.CM, objectMm = 120, toleranceMm = 2))
    }

    @Test
    fun `lectura en milimetros`() {
        assertTrue(MeasureEngine.checkRulerReading(32.0, LengthUnit.MM, objectMm = 32, toleranceMm = 2))
        assertFalse(MeasureEngine.checkRulerReading(3.2, LengthUnit.MM, objectMm = 32, toleranceMm = 2))
    }

    @Test
    fun `etiqueta amable de longitud`() {
        assertEquals("4,5 cm", MeasureEngine.prettyLength(45, LengthUnit.CM))
        assertEquals("32 mm", MeasureEngine.prettyLength(32, LengthUnit.MM))
        assertEquals("12 cm", MeasureEngine.prettyLength(120, LengthUnit.CM))
    }

    @Test
    fun `la balanza se equilibra con la suma exacta`() {
        assertTrue(MeasureEngine.isBalanced(300, listOf(200, 100)))
        assertFalse(MeasureEngine.isBalanced(300, listOf(200, 50)))
        assertFalse(MeasureEngine.isBalanced(300, emptyList()))
    }

    @Test
    fun `una masa de cero nunca se considera equilibrada`() {
        assertFalse(MeasureEngine.isBalanced(0, emptyList()))
    }

    @Test
    fun `inclinacion de la balanza`() {
        assertEquals(-1, MeasureEngine.tilt(500, 200))
        assertEquals(1, MeasureEngine.tilt(200, 500))
        assertEquals(0, MeasureEngine.tilt(500, 500))
    }

    @Test
    fun `diferencia positiva cuando falta peso`() {
        assertEquals(150, MeasureEngine.difference(750, listOf(500, 100)))
        assertEquals(-50, MeasureEngine.difference(750, listOf(500, 200, 100)))
    }

    @Test
    fun `sugerencia de pesa util`() {
        assertEquals(200, MeasureEngine.suggestWeight(750, listOf(500), listOf(500, 200, 100, 50)))
        assertNull(MeasureEngine.suggestWeight(750, listOf(500, 200, 50), listOf(500, 200, 100, 50)))
    }

    @Test
    fun `solucion voraz de la balanza`() {
        assertEquals(listOf(500, 200, 50), MeasureEngine.greedySolution(750, listOf(500, 200, 100, 50)))
        assertEquals(listOf(1000, 1000, 200, 200), MeasureEngine.greedySolution(2400, listOf(1000, 500, 200, 100)))
        assertTrue(MeasureEngine.greedySolution(0, listOf(100)).isEmpty())
    }
}
