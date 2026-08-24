package com.matelab.islas.content

import com.matelab.islas.data.local.MateJson
import com.matelab.islas.domain.content.Catalog
import com.matelab.islas.domain.model.ChallengePayload
import com.matelab.islas.domain.model.FractionPiePayload
import com.matelab.islas.domain.model.GeoObjective
import com.matelab.islas.domain.model.GeoboardPayload
import com.matelab.islas.domain.model.PieShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La configuracion de cada mini-juego viaja a la base de datos como JSON.
 * Si esta conversion se rompe, la app se queda sin retos.
 */
class PayloadSerializationTest {

    @Test
    fun `un payload de geoplano sobrevive al viaje de ida y vuelta`() {
        val original: ChallengePayload = GeoboardPayload(
            grid = 6, objective = GeoObjective.AREA, target = 6.0, unitLabel = "cuadraditos"
        )
        val json = MateJson.encode(original)
        assertEquals(original, MateJson.decode(json))
    }

    @Test
    fun `el json lleva el discriminador de tipo`() {
        val json = MateJson.encode(
            FractionPiePayload(PieShape.BARRA, 8, 5, 8)
        )
        assertTrue("Falta el campo type en: $json", json.contains("\"type\""))
        assertTrue(json.contains("fraction_pie"))
    }

    @Test
    fun `un json corrupto no revienta la app`() {
        assertNull(MateJson.decodeOrNull("{esto no es json}"))
        assertNull(MateJson.decodeOrNull(""))
    }

    @Test
    fun `todos los retos del catalogo se serializan y recuperan`() {
        Catalog.challenges.forEach { challenge ->
            val json = MateJson.encode(challenge.payload)
            val recuperado = MateJson.decode(json)
            assertEquals("Fallo en ${challenge.id}", challenge.payload, recuperado)
        }
    }

    @Test
    fun `el json de un reto no es exageradamente grande`() {
        Catalog.challenges.forEach { challenge ->
            val json = MateJson.encode(challenge.payload)
            assertTrue(
                "El reto ${challenge.id} ocupa ${json.length} caracteres",
                json.length < 4000
            )
        }
    }
}
