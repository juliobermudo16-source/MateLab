package com.matelab.islas.content

import com.matelab.islas.domain.content.Catalog
import com.matelab.islas.domain.engine.MeasureEngine
import com.matelab.islas.domain.engine.PatternEngine
import com.matelab.islas.domain.engine.ShapeSortEngine
import com.matelab.islas.domain.engine.SymmetryEngine
import com.matelab.islas.domain.model.AngleDialPayload
import com.matelab.islas.domain.model.BalancePayload
import com.matelab.islas.domain.model.ClockPayload
import com.matelab.islas.domain.model.FractionLinePayload
import com.matelab.islas.domain.model.FractionPiePayload
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.GeoObjective
import com.matelab.islas.domain.model.GeoboardPayload
import com.matelab.islas.domain.model.PatternPayload
import com.matelab.islas.domain.model.PlaceValuePayload
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.RulerPayload
import com.matelab.islas.domain.model.ShapeSortPayload
import com.matelab.islas.domain.model.SymmetryPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Control de calidad del contenido educativo.
 *
 * Estas pruebas evitan que un reto imposible o mal configurado llegue al nino.
 */
class CatalogIntegrityTest {

    @Test
    fun `el catalogo no tiene problemas de integridad`() {
        val problemas = Catalog.integrityProblems()
        assertTrue("Problemas encontrados: $problemas", problemas.isEmpty())
    }

    @Test
    fun `hay contenido suficiente para varias sesiones`() {
        assertEquals(4, Catalog.worlds.size)
        assertTrue("Se esperaban al menos 16 misiones", Catalog.missions.size >= 16)
        assertTrue("Se esperaban al menos 80 retos", Catalog.challenges.size >= 80)
        assertTrue("Se esperaban al menos 10 insignias", Catalog.badges.size >= 10)
        assertTrue("Se esperaban al menos 20 cristales", Catalog.collectibles.size >= 20)
    }

    @Test
    fun `cada isla tiene al menos cuatro misiones`() {
        Catalog.worlds.forEach { world ->
            val misiones = Catalog.missionsOf(world.id)
            assertTrue("La isla ${world.id} tiene ${misiones.size} misiones", misiones.size >= 4)
        }
    }

    @Test
    fun `las islas se desbloquean en orden creciente de experiencia`() {
        val umbrales = Catalog.worlds.sortedBy { it.order }.map { it.xpToUnlock }
        assertEquals(0, umbrales.first())
        assertEquals(umbrales.sorted(), umbrales)
    }

    @Test
    fun `los mini juegos de manipulacion son mayoria`() {
        val total = Catalog.challenges.size
        val tests = Catalog.challenges.count { it.kind == GameKind.QUIZ }
        assertTrue(
            "Demasiados retos de eleccion: $tests de $total",
            tests * 100 / total < 50
        )
    }

    @Test
    fun `se usan al menos diez tipos de mini juego distintos`() {
        val tipos = Catalog.challenges.map { it.kind }.toSet()
        assertTrue("Solo hay ${tipos.size} tipos", tipos.size >= 10)
    }

    @Test
    fun `todas las misiones premian un cristal distinto`() {
        val premios = Catalog.missions.mapNotNull { it.rewardCollectibleId }
        assertEquals(premios.size, premios.toSet().size)
    }

    @Test
    fun `los retos del geoplano son alcanzables`() {
        Catalog.challenges.mapNotNull { it.payload as? GeoboardPayload }.forEach { p ->
            assertTrue("Malla demasiado pequena", p.grid in 4..10)
            when (p.objective) {
                GeoObjective.AREA -> assertTrue(
                    "Area imposible en una malla de ${p.grid}",
                    p.target > 0 && p.target <= p.grid * p.grid
                )
                GeoObjective.PERIMETRO -> assertTrue(
                    "Perimetro imposible",
                    p.target >= 3 && p.target <= 4 * p.grid
                )
                GeoObjective.LADOS -> assertTrue(
                    "Numero de lados poco razonable",
                    p.target.toInt() in 3..8
                )
            }
        }
    }

    @Test
    fun `los retos de clasificar tienen solucion unica`() {
        Catalog.challenges.mapNotNull { it.payload as? ShapeSortPayload }.forEach { p ->
            assertTrue(
                "Reto de clasificar mal formado",
                ShapeSortEngine.isChallengeWellFormed(p.shapes, p.buckets)
            )
            // Ninguna figura puede encajar en dos cestas a la vez.
            p.shapes.forEach { shape ->
                val encajes = p.buckets.count { ShapeSortEngine.matches(shape, it) }
                assertEquals("La figura ${shape.id} encaja en $encajes cestas", 1, encajes)
            }
        }
    }

    @Test
    fun `los mosaicos de simetria estan bien planteados`() {
        Catalog.challenges.mapNotNull { it.payload as? SymmetryPayload }.forEach { p ->
            val celdas = p.rows * p.cols
            p.given.forEach { index ->
                assertTrue("Celda fuera de la cuadricula: $index", index in 0 until celdas)
                assertTrue(
                    "La celda $index deberia estar en la mitad de partida",
                    !SymmetryEngine.isEditable(index, p.rows, p.cols, p.axis, emptyList())
                )
            }
            val esperadas = SymmetryEngine.expectedCells(p.given, p.rows, p.cols, p.axis)
            assertTrue("El mosaico no pide pintar nada", esperadas.isNotEmpty())
            esperadas.forEach { index ->
                assertTrue(
                    "La celda $index deberia poder pintarse",
                    SymmetryEngine.isEditable(index, p.rows, p.cols, p.axis, p.given)
                )
            }
        }
    }

    @Test
    fun `los angulos pedidos son medibles`() {
        Catalog.challenges.mapNotNull { it.payload as? AngleDialPayload }.forEach { p ->
            assertTrue("Angulo fuera de rango", p.targetDegrees in 0..360)
            assertTrue("Tolerancia poco razonable", p.tolerance in 1..10)
        }
    }

    @Test
    fun `los objetos que se miden caben en la regla`() {
        Catalog.challenges.mapNotNull { it.payload as? RulerPayload }.forEach { p ->
            assertTrue("Objeto de longitud invalida", p.objectMm in 5..160)
            assertTrue("Unidad de respuesta desconocida", p.answerUnit in listOf("cm", "mm"))
            assertTrue("Tolerancia poco razonable", p.toleranceMm in 1..5)
        }
    }

    @Test
    fun `todas las balanzas se pueden equilibrar`() {
        Catalog.challenges.mapNotNull { it.payload as? BalancePayload }.forEach { p ->
            val solucion = MeasureEngine.greedySolution(p.leftGrams, p.weights)
            assertTrue("Balanza sin solucion: ${p.leftLabel}", solucion.isNotEmpty())
            assertTrue(
                "La solucion no equilibra: ${p.leftLabel}",
                MeasureEngine.isBalanced(p.leftGrams, solucion)
            )
            val maximoRepetido = solucion.groupBy { it }.maxOf { it.value.size }
            assertTrue(
                "Hacen falta $maximoRepetido pesas iguales y solo hay ${p.maxPerWeight}",
                maximoRepetido <= p.maxPerWeight
            )
        }
    }

    @Test
    fun `las horas de los relojes son validas`() {
        Catalog.challenges.mapNotNull { it.payload as? ClockPayload }.forEach { p ->
            assertTrue(p.startHour in 0..23)
            assertTrue(p.startMinute in 0..59)
            assertTrue(p.targetHour in 0..23)
            assertTrue(p.targetMinute in 0..59)
        }
    }

    @Test
    fun `las fracciones pedidas se pueden pintar`() {
        Catalog.challenges.mapNotNull { it.payload as? FractionPiePayload }.forEach { p ->
            assertTrue("Numero de partes poco razonable", p.parts in 2..16)
            assertTrue("Denominador invalido", p.targetDenominator > 0)
            val existeSolucion = (0..p.parts).any { pintadas ->
                pintadas.toLong() * p.targetDenominator == p.targetNumerator.toLong() * p.parts
            }
            assertTrue(
                "No hay forma de pintar ${p.targetNumerator}/${p.targetDenominator} con ${p.parts} partes",
                existeSolucion
            )
        }
    }

    @Test
    fun `las fracciones de la recta caben en el tramo dibujado`() {
        Catalog.challenges.mapNotNull { it.payload as? FractionLinePayload }.forEach { p ->
            assertTrue("Denominador invalido", p.denominator > 0)
            assertTrue("Unidades invalidas", p.wholes > 0)
            assertTrue(
                "La fraccion ${p.numerator}/${p.denominator} se sale de la recta",
                p.numerator in 0..(p.denominator * p.wholes)
            )
        }
    }

    @Test
    fun `los numeros de los bloques se pueden construir`() {
        Catalog.challenges.mapNotNull { it.payload as? PlaceValuePayload }.forEach { p ->
            assertTrue("Numero fuera de rango", p.target in 1..9999)
            val maximo = p.pieces.sumOf { it * p.maxPerPiece }
            assertTrue("No hay piezas suficientes para ${p.target}", maximo >= p.target)
        }
    }

    @Test
    fun `la respuesta de cada patron coincide con su regla`() {
        Catalog.challenges.mapNotNull { it.payload as? PatternPayload }.forEach { p ->
            assertTrue("Indice de respuesta invalido", p.answerIndex in p.options.indices)
            assertTrue("Hueco fuera de la secuencia", p.holeIndex in p.sequence.indices)
            val esperado = p.options[p.answerIndex]

            val esNumerica = p.sequence.all { it.shape == null }
            if (esNumerica) {
                val valores = p.sequence.mapIndexed { index, token ->
                    if (index == p.holeIndex) null else token.label.toIntOrNull()
                }
                val calculado = PatternEngine.fillHole(valores)
                assertNotNull("No se deduce la regla de: ${p.rule}", calculado)
                assertEquals(
                    "La opcion correcta no sigue la regla: ${p.rule}",
                    calculado.toString(), esperado.label
                )
            } else {
                val tokens = p.sequence.mapIndexed { index, token ->
                    if (index == p.holeIndex) null else "${token.shape}:${token.colorIndex}"
                }
                val calculado = PatternEngine.predictHole(tokens)
                assertNotNull("No se deduce el patron de: ${p.rule}", calculado)
                assertEquals(
                    "La opcion correcta rompe el patron: ${p.rule}",
                    calculado, "${esperado.shape}:${esperado.colorIndex}"
                )
            }
        }
    }

    @Test
    fun `los retos de eleccion tienen opciones validas`() {
        Catalog.challenges.mapNotNull { it.payload as? QuizPayload }.forEach { p ->
            assertTrue("Faltan opciones", p.options.size >= 3)
            assertTrue("Respuesta fuera de rango", p.answerIndex in p.options.indices)
            assertEquals("Hay opciones repetidas", p.options.size, p.options.toSet().size)
            assertTrue("Hay opciones vacias", p.options.none { it.isBlank() })
        }
    }

    @Test
    fun `los textos son breves y estan en espanol`() {
        Catalog.challenges.forEach { challenge ->
            assertTrue(
                "Enunciado demasiado largo en ${challenge.id}",
                challenge.prompt.length <= 130
            )
            assertTrue(
                "Explicacion demasiado larga en ${challenge.id}",
                challenge.explanation.length <= 260
            )
            assertTrue(
                "Pista demasiado larga en ${challenge.id}",
                challenge.hint.length <= 130
            )
        }
    }

    @Test
    fun `cada cristal pertenece a una isla real`() {
        val islas = Catalog.worlds.map { it.id }.toSet()
        Catalog.collectibles.forEach { crystal ->
            assertTrue("Cristal ${crystal.id} sin isla", crystal.worldId in islas)
            assertTrue("Cristal ${crystal.id} sin dato curioso", crystal.fact.isNotBlank())
        }
    }

    @Test
    fun `las semillas de dibujo no se repiten`() {
        val semillasCristales = Catalog.collectibles.map { it.artSeed }
        assertEquals(semillasCristales.size, semillasCristales.toSet().size)
        val semillasInsignias = Catalog.badges.map { it.artSeed }
        assertEquals(semillasInsignias.size, semillasInsignias.toSet().size)
    }
}
