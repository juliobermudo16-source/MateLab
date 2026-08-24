package com.matelab.islas.engine

import com.matelab.islas.domain.engine.Fraction
import com.matelab.islas.domain.engine.FractionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FractionEngineTest {

    @Test
    fun `simplifica una fraccion reducible`() {
        assertEquals(Fraction(2, 3), Fraction(4, 6).simplified())
    }

    @Test
    fun `simplificar cero da cero partido uno`() {
        assertEquals(Fraction(0, 1), Fraction(0, 7).simplified())
    }

    @Test
    fun `el signo se mueve siempre al numerador`() {
        val f = Fraction(3, -4)
        assertEquals(-3, f.n)
        assertEquals(4, f.d)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `denominador cero lanza excepcion`() {
        Fraction(1, 0)
    }

    @Test
    fun `dos fracciones distintas pueden ser equivalentes`() {
        assertTrue(Fraction(3, 4).isEquivalentTo(Fraction(6, 8)))
        assertFalse(Fraction(3, 4).isEquivalentTo(Fraction(2, 3)))
    }

    @Test
    fun `suma con denominadores distintos`() {
        assertEquals(Fraction(5, 6), Fraction(1, 2) + Fraction(1, 3))
    }

    @Test
    fun `resta que da cero`() {
        assertEquals(Fraction(0, 1), Fraction(2, 4) - Fraction(1, 2))
    }

    @Test
    fun `comparar con mismo denominador gana el numerador mayor`() {
        assertEquals(1, FractionEngine.compare(Fraction(3, 5), Fraction(2, 5)))
    }

    @Test
    fun `comparar con mismo numerador gana el denominador menor`() {
        assertEquals(1, FractionEngine.compare(Fraction(1, 3), Fraction(1, 5)))
    }

    @Test
    fun `simbolo de comparacion`() {
        assertEquals("<", FractionEngine.comparisonSymbol(Fraction(1, 4), Fraction(1, 2)))
        assertEquals("=", FractionEngine.comparisonSymbol(Fraction(2, 4), Fraction(1, 2)))
        assertEquals(">", FractionEngine.comparisonSymbol(Fraction(7, 8), Fraction(1, 2)))
    }

    @Test
    fun `equivalentes hasta un denominador maximo`() {
        val list = FractionEngine.equivalents(Fraction(1, 2), 8)
        assertEquals(listOf(Fraction(1, 2), Fraction(2, 4), Fraction(3, 6), Fraction(4, 8)), list)
    }

    @Test
    fun `pintar porciones acepta cualquier equivalente`() {
        assertTrue(FractionEngine.checkPainted(painted = 4, parts = 8, targetNumerator = 1, targetDenominator = 2))
        assertTrue(FractionEngine.checkPainted(painted = 6, parts = 8, targetNumerator = 3, targetDenominator = 4))
        assertFalse(FractionEngine.checkPainted(painted = 5, parts = 8, targetNumerator = 1, targetDenominator = 2))
    }

    @Test
    fun `pintar mas porciones de las que hay es incorrecto`() {
        assertFalse(FractionEngine.checkPainted(painted = 9, parts = 8, targetNumerator = 9, targetDenominator = 8))
    }

    @Test
    fun `pintar un numero negativo de porciones es incorrecto`() {
        assertFalse(FractionEngine.checkPainted(painted = -1, parts = 4, targetNumerator = 1, targetDenominator = 4))
    }

    @Test
    fun `posicion en la recta se recorta al rango valido`() {
        assertEquals(0.5, FractionEngine.positionOnLine(Fraction(1, 2), 1), 0.0001)
        assertEquals(1.0, FractionEngine.positionOnLine(Fraction(9, 4), 1), 0.0001)
    }

    @Test
    fun `imantar la posicion a la marca mas cercana`() {
        assertEquals(3, FractionEngine.snapToStep(0.74, denominator = 4, wholes = 1))
        assertEquals(0, FractionEngine.snapToStep(-5.0, denominator = 4, wholes = 1))
        assertEquals(4, FractionEngine.snapToStep(2.0, denominator = 4, wholes = 1))
    }

    @Test
    fun `la recta admite tolerancia de marcas`() {
        assertTrue(FractionEngine.checkLine(chosenStep = 4, targetNumerator = 5, toleranceSteps = 1))
        assertFalse(FractionEngine.checkLine(chosenStep = 3, targetNumerator = 5, toleranceSteps = 1))
    }

    @Test
    fun `etiqueta de numero mixto`() {
        assertEquals("1 1/4", FractionEngine.mixedLabel(Fraction(5, 4)))
        assertEquals("3/4", FractionEngine.mixedLabel(Fraction(3, 4)))
        assertEquals("2", FractionEngine.mixedLabel(Fraction(8, 4)))
    }

    @Test
    fun `maximo comun divisor y minimo comun multiplo`() {
        assertEquals(6, FractionEngine.gcd(12, 18))
        assertEquals(1, FractionEngine.gcd(0, 0))
        assertEquals(12, FractionEngine.lcm(4, 6))
        assertEquals(0, FractionEngine.lcm(0, 5))
    }

    @Test
    fun `denominador comun para explicar la suma`() {
        assertEquals(8, FractionEngine.commonDenominator(Fraction(1, 8), Fraction(1, 4)))
    }
}
