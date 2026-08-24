package com.matelab.islas.engine

import com.matelab.islas.domain.engine.AngleEngine
import com.matelab.islas.domain.engine.AngleKind
import com.matelab.islas.domain.engine.ClockEngine
import com.matelab.islas.domain.engine.ClockTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAndAngleTest {

    // ------------------------------------------------------------- angulos

    @Test
    fun `normalizar angulos negativos y grandes`() {
        assertEquals(270.0, AngleEngine.normalize(-90.0), 0.0001)
        assertEquals(30.0, AngleEngine.normalize(390.0), 0.0001)
        assertEquals(0.0, AngleEngine.normalize(720.0), 0.0001)
    }

    @Test
    fun `clasificar cada tipo de angulo`() {
        assertEquals(AngleKind.AGUDO, AngleEngine.classify(45.0))
        assertEquals(AngleKind.RECTO, AngleEngine.classify(90.0))
        assertEquals(AngleKind.OBTUSO, AngleEngine.classify(135.0))
        assertEquals(AngleKind.LLANO, AngleEngine.classify(180.0))
        assertEquals(AngleKind.REFLEJO, AngleEngine.classify(270.0))
        assertEquals(AngleKind.COMPLETO, AngleEngine.classify(360.0))
    }

    @Test
    fun `diferencia mas corta entre angulos`() {
        assertEquals(20.0, AngleEngine.shortestDelta(350.0, 10.0), 0.0001)
        assertEquals(180.0, AngleEngine.shortestDelta(0.0, 180.0), 0.0001)
    }

    @Test
    fun `tolerancia del transportador`() {
        assertTrue(AngleEngine.withinTolerance(88.0, 90.0, 4))
        assertFalse(AngleEngine.withinTolerance(80.0, 90.0, 4))
        assertTrue(AngleEngine.withinTolerance(358.0, 2.0, 5))
    }

    @Test
    fun `imantar el giro a multiplos`() {
        assertEquals(45.0, AngleEngine.snap(43.0, 15), 0.0001)
        assertEquals(90.0, AngleEngine.snap(87.4, 1), 0.0001)
    }

    @Test
    fun `angulo del vector hacia el este es cero`() {
        assertEquals(0.0, AngleEngine.angleOf(1.0, 0.0), 0.0001)
        assertEquals(90.0, AngleEngine.angleOf(0.0, -1.0), 0.0001)
    }

    @Test
    fun `suma de angulos interiores`() {
        assertEquals(180, AngleEngine.interiorAngleSum(3))
        assertEquals(360, AngleEngine.interiorAngleSum(4))
        assertEquals(720, AngleEngine.interiorAngleSum(6))
        assertEquals(120.0, AngleEngine.regularInteriorAngle(6), 0.0001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un poligono de dos lados no existe`() {
        AngleEngine.interiorAngleSum(2)
    }

    // --------------------------------------------------------------- reloj

    @Test
    fun `angulos de las manecillas`() {
        assertEquals(90.0, ClockEngine.minuteHandAngle(15), 0.0001)
        assertEquals(0.0, ClockEngine.hourHandAngle(12, 0), 0.0001)
        assertEquals(97.5, ClockEngine.hourHandAngle(3, 15), 0.0001)
    }

    @Test
    fun `de angulo a minuto y a hora`() {
        assertEquals(15, ClockEngine.minuteFromAngle(90.0))
        assertEquals(0, ClockEngine.minuteFromAngle(359.0))
        assertEquals(3, ClockEngine.hourFromAngle(95.0))
    }

    @Test
    fun `avanzar minutos cambia de hora`() {
        val resultado = ClockEngine.addMinutes(ClockTime(2, 50), 25)
        assertEquals(3, resultado.hour)
        assertEquals(15, resultado.minute)
    }

    @Test
    fun `avanzar mas de una hora`() {
        val resultado = ClockEngine.addMinutes(ClockTime(5, 20), 70)
        assertEquals(6, resultado.hour)
        assertEquals(30, resultado.minute)
    }

    @Test
    fun `retroceder minutos da la vuelta al reloj`() {
        val resultado = ClockEngine.addMinutes(ClockTime(0, 10), -20)
        assertEquals(11, resultado.hour)
        assertEquals(50, resultado.minute)
    }

    @Test
    fun `duracion entre dos horas`() {
        assertEquals(45, ClockEngine.durationMinutes(ClockTime(9, 30), ClockTime(10, 15)))
        assertEquals(60, ClockEngine.durationMinutes(ClockTime(11, 30), ClockTime(0, 30)))
    }

    @Test
    fun `comparar horas ignora las 12 y las 0`() {
        assertTrue(ClockEngine.matches(ClockTime(0, 15), ClockTime(12, 15)))
        assertFalse(ClockEngine.matches(ClockTime(3, 15), ClockTime(3, 20)))
        assertTrue(ClockEngine.matches(ClockTime(3, 15), ClockTime(3, 20), toleranceMinutes = 5))
    }

    @Test
    fun `etiqueta de hora y de duracion`() {
        assertEquals("3:05", ClockTime(3, 5).label())
        assertEquals("12:00", ClockTime(0, 0).label())
        assertEquals("1 h 25 min", ClockEngine.durationLabel(85))
        assertEquals("45 min", ClockEngine.durationLabel(45))
        assertEquals("2 h", ClockEngine.durationLabel(120))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `un minuto fuera de rango no se acepta`() {
        ClockTime(3, 75)
    }
}
