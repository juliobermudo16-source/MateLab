package com.matelab.islas.progress

import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressEngineTest {

    private val isla = World(
        id = "w1", order = 1, name = "Isla", subtitle = "", description = "",
        theme = WorldTheme.FORMAS, xpToUnlock = 100
    )

    private fun mision(id: String, requiere: List<String> = emptyList()) = Mission(
        id = id, worldId = "w1", order = 1, name = id, goal = "", briefing = "",
        difficulty = Difficulty.EXPLORADOR, requires = requiere
    )

    @Test
    fun `el nivel uno empieza en cero experiencia`() {
        assertEquals(0, ProgressEngine.cumulativeXpFor(1))
        assertEquals(1, ProgressEngine.levelFor(0))
        assertEquals(1, ProgressEngine.levelFor(-50))
    }

    @Test
    fun `los niveles cuestan cada vez mas`() {
        assertEquals(100, ProgressEngine.cumulativeXpFor(2))
        assertEquals(250, ProgressEngine.cumulativeXpFor(3))
        assertEquals(450, ProgressEngine.cumulativeXpFor(4))
    }

    @Test
    fun `el nivel se deduce de la experiencia`() {
        assertEquals(1, ProgressEngine.levelFor(99))
        assertEquals(2, ProgressEngine.levelFor(100))
        assertEquals(3, ProgressEngine.levelFor(300))
    }

    @Test
    fun `experiencia que falta para el siguiente nivel`() {
        assertEquals(100, ProgressEngine.xpToNextLevel(0))
        assertEquals(1, ProgressEngine.xpToNextLevel(99))
        assertEquals(150, ProgressEngine.xpToNextLevel(100))
    }

    @Test
    fun `progreso dentro del nivel entre cero y uno`() {
        assertEquals(0f, ProgressEngine.levelProgress(0), 0.0001f)
        assertEquals(0.5f, ProgressEngine.levelProgress(50), 0.0001f)
        assertTrue(ProgressEngine.levelProgress(120) in 0f..1f)
    }

    @Test
    fun `tres estrellas exigen pleno y sin pistas`() {
        assertEquals(3, ProgressEngine.starsFor(correct = 5, total = 5, hintsUsed = 0))
        assertEquals(2, ProgressEngine.starsFor(correct = 5, total = 5, hintsUsed = 1))
    }

    @Test
    fun `estrellas segun el porcentaje`() {
        assertEquals(2, ProgressEngine.starsFor(4, 5, 0))
        assertEquals(1, ProgressEngine.starsFor(3, 5, 0))
        assertEquals(0, ProgressEngine.starsFor(1, 5, 0))
    }

    @Test
    fun `una mision sin retos no da estrellas`() {
        assertEquals(0, ProgressEngine.starsFor(0, 0, 0))
    }

    @Test
    fun `mas aciertos de los posibles no inflan la nota`() {
        assertEquals(3, ProgressEngine.starsFor(correct = 99, total = 5, hintsUsed = 0))
    }

    @Test
    fun `la experiencia depende de la dificultad`() {
        assertEquals(65, ProgressEngine.xpFor(5, 3, Difficulty.EXPLORADOR))
        assertEquals(115, ProgressEngine.xpFor(5, 3, Difficulty.AVENTURERO))
        assertEquals(165, ProgressEngine.xpFor(5, 3, Difficulty.MAESTRO))
    }

    @Test
    fun `sin aciertos casi no hay experiencia`() {
        assertEquals(0, ProgressEngine.xpFor(0, 0, Difficulty.EXPLORADOR))
        assertEquals(0, ProgressEngine.xpFor(-3, 0, Difficulty.EXPLORADOR))
    }

    @Test
    fun `una isla se abre con la experiencia suficiente`() {
        assertFalse(ProgressEngine.isWorldUnlocked(isla, 99))
        assertTrue(ProgressEngine.isWorldUnlocked(isla, 100))
    }

    @Test
    fun `una mision necesita sus requisitos`() {
        val m = mision("m2", listOf("m1"))
        assertFalse(ProgressEngine.isMissionUnlocked(m, worldUnlocked = true, completedMissionIds = emptySet()))
        assertTrue(ProgressEngine.isMissionUnlocked(m, worldUnlocked = true, completedMissionIds = setOf("m1")))
        assertFalse(ProgressEngine.isMissionUnlocked(m, worldUnlocked = false, completedMissionIds = setOf("m1")))
    }

    @Test
    fun `estados visibles de una mision`() {
        val m = mision("m1")
        assertEquals(
            MissionStatus.BLOQUEADA,
            ProgressEngine.statusFor(m, null, worldUnlocked = false, completedMissionIds = emptySet())
        )
        assertEquals(
            MissionStatus.DISPONIBLE,
            ProgressEngine.statusFor(m, null, worldUnlocked = true, completedMissionIds = emptySet())
        )
        assertEquals(
            MissionStatus.EMPEZADA,
            ProgressEngine.statusFor(
                m, MissionProgress("m1", stars = 0, timesPlayed = 1), true, emptySet()
            )
        )
        assertEquals(
            MissionStatus.COMPLETADA,
            ProgressEngine.statusFor(
                m, MissionProgress("m1", stars = 2, timesPlayed = 1), true, emptySet()
            )
        )
        assertEquals(
            MissionStatus.DOMINADA,
            ProgressEngine.statusFor(
                m, MissionProgress("m1", stars = 3, timesPlayed = 2), true, emptySet()
            )
        )
    }

    @Test
    fun `porcentaje de una isla por estrellas`() {
        val progreso = mapOf(
            "m1" to MissionProgress("m1", stars = 3),
            "m2" to MissionProgress("m2", stars = 3)
        )
        assertEquals(100, ProgressEngine.worldPercent(listOf("m1", "m2"), progreso))
        assertEquals(50, ProgressEngine.worldPercent(listOf("m1", "m2", "m3", "m4"), progreso))
        assertEquals(0, ProgressEngine.worldPercent(emptyList(), progreso))
    }

    @Test
    fun `repetir peor no baja el mejor resultado`() {
        val antes = MissionProgress("m1", stars = 3, bestPercent = 100, timesPlayed = 1)
        val despues = ProgressEngine.mergeProgress(antes, "m1", stars = 1, percent = 40, now = 10L)
        assertEquals(3, despues.stars)
        assertEquals(100, despues.bestPercent)
        assertEquals(2, despues.timesPlayed)
        assertEquals(MissionStatus.DOMINADA, despues.status)
    }

    @Test
    fun `la primera partida crea el progreso`() {
        val nuevo = ProgressEngine.mergeProgress(null, "m1", stars = 2, percent = 80, now = 5L)
        assertEquals(2, nuevo.stars)
        assertEquals(1, nuevo.timesPlayed)
        assertEquals(5L, nuevo.lastPlayedAt)
    }

    @Test
    fun `la racha sube al dia siguiente y se reinicia si se salta`() {
        assertEquals(1, ProgressEngine.nextStreak(previousStreak = 0, lastPlayedDay = 0L, today = 20L))
        assertEquals(4, ProgressEngine.nextStreak(previousStreak = 3, lastPlayedDay = 19L, today = 20L))
        assertEquals(3, ProgressEngine.nextStreak(previousStreak = 3, lastPlayedDay = 20L, today = 20L))
        assertEquals(1, ProgressEngine.nextStreak(previousStreak = 7, lastPlayedDay = 10L, today = 20L))
    }

    @Test
    fun `una mision cuenta como completada desde una estrella`() {
        assertFalse(ProgressEngine.isCompleted(null))
        assertFalse(ProgressEngine.isCompleted(MissionProgress("m1", stars = 0)))
        assertTrue(ProgressEngine.isCompleted(MissionProgress("m1", stars = 1)))
    }
}
