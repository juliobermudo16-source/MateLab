package com.matelab.islas.progress

import com.matelab.islas.data.repository.PlayerRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El apodo lo escribe un nino: puede llegar vacio, con espacios de sobra o
 * kilometrico. Nunca debe romper la interfaz.
 */
class AliasSanitizerTest {

    @Test
    fun `se recortan los espacios sobrantes`() {
        assertEquals("Nova", PlayerRepositoryImpl.sanitizeAlias("   Nova   "))
        assertEquals("Nova Kubo", PlayerRepositoryImpl.sanitizeAlias("Nova     Kubo"))
    }

    @Test
    fun `los saltos de linea desaparecen`() {
        assertEquals("Nova Kubo", PlayerRepositoryImpl.sanitizeAlias("Nova\n\nKubo"))
    }

    @Test
    fun `un apodo kilometrico se corta`() {
        val largo = "A".repeat(200)
        val resultado = PlayerRepositoryImpl.sanitizeAlias(largo)
        assertEquals(PlayerRepositoryImpl.MAX_ALIAS_LENGTH, resultado.length)
    }

    @Test
    fun `un apodo vacio sigue siendo vacio`() {
        assertTrue(PlayerRepositoryImpl.sanitizeAlias("      ").isEmpty())
        assertTrue(PlayerRepositoryImpl.sanitizeAlias("").isEmpty())
    }

    @Test
    fun `hay ocho avatares disponibles`() {
        assertEquals(8, PlayerRepositoryImpl.AVATAR_COUNT)
    }
}
