package com.matelab.islas.progress

import com.matelab.islas.domain.engine.BadgeContext
import com.matelab.islas.domain.engine.BadgeEngine
import com.matelab.islas.domain.engine.CollectibleEngine
import com.matelab.islas.domain.engine.ReviewEngine
import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.BadgeRule
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.Rarity
import com.matelab.islas.domain.model.ReviewItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardEnginesTest {

    // ------------------------------------------------------------ insignias

    private val primera = Badge("b1", "Primera", "", BadgeRule.FIRST_MISSION, 1, null, 1)
    private val estrellas = Badge("b2", "Estrellas", "", BadgeRule.TOTAL_STARS, 25, null, 2)
    private val islaCompleta = Badge("b3", "Isla", "", BadgeRule.WORLD_COMPLETE, 0, "w1", 3)
    private val maestro = Badge("b4", "Maestro", "", BadgeRule.TOPIC_MASTER, 85, "w1", 4)

    @Test
    fun `la primera mision desbloquea la insignia inicial`() {
        assertFalse(BadgeEngine.isEarned(primera, BadgeContext()))
        assertTrue(BadgeEngine.isEarned(primera, BadgeContext(missionsCompleted = 1)))
    }

    @Test
    fun `insignia por numero de estrellas`() {
        assertFalse(BadgeEngine.isEarned(estrellas, BadgeContext(totalStars = 24)))
        assertTrue(BadgeEngine.isEarned(estrellas, BadgeContext(totalStars = 25)))
    }

    @Test
    fun `insignia de isla completa`() {
        val parcial = BadgeContext(
            completedByWorld = mapOf("w1" to 3),
            totalByWorld = mapOf("w1" to 5)
        )
        val completa = BadgeContext(
            completedByWorld = mapOf("w1" to 5),
            totalByWorld = mapOf("w1" to 5)
        )
        assertFalse(BadgeEngine.isEarned(islaCompleta, parcial))
        assertTrue(BadgeEngine.isEarned(islaCompleta, completa))
    }

    @Test
    fun `una isla sin misiones nunca se da por completada`() {
        val vacia = BadgeContext(completedByWorld = mapOf("w1" to 0), totalByWorld = mapOf("w1" to 0))
        assertFalse(BadgeEngine.isEarned(islaCompleta, vacia))
    }

    @Test
    fun `insignia de dominio exige precision y practica`() {
        val pocoJugado = BadgeContext(
            accuracyByWorld = mapOf("w1" to 95),
            completedByWorld = mapOf("w1" to 1)
        )
        val dominado = BadgeContext(
            accuracyByWorld = mapOf("w1" to 90),
            completedByWorld = mapOf("w1" to 3)
        )
        assertFalse(BadgeEngine.isEarned(maestro, pocoJugado))
        assertTrue(BadgeEngine.isEarned(maestro, dominado))
    }

    @Test
    fun `solo se entregan las insignias nuevas`() {
        val ctx = BadgeContext(missionsCompleted = 1, totalStars = 30)
        val nuevas = BadgeEngine.newlyEarned(listOf(primera, estrellas), setOf("b1"), ctx)
        assertEquals(listOf("b2"), nuevas.map { it.id })
    }

    @Test
    fun `avance de una insignia con umbral`() {
        assertEquals(0.4f, BadgeEngine.progressOf(estrellas, BadgeContext(totalStars = 10)), 0.0001f)
        assertEquals(1f, BadgeEngine.progressOf(estrellas, BadgeContext(totalStars = 40)), 0.0001f)
        assertEquals("40 %", BadgeEngine.progressLabel(estrellas, BadgeContext(totalStars = 10)))
    }

    // ------------------------------------------------------------ cristales

    private val mision = Mission(
        id = "m1", worldId = "w1", order = 1, name = "Mision", goal = "", briefing = "",
        difficulty = Difficulty.EXPLORADOR, rewardCollectibleId = "cr1"
    )

    private val hitos = listOf(
        Collectible("cr_hito_formas", "Sello", "", "w_formas", Rarity.LEGENDARIO, 1),
        Collectible("cr_hito_nivel5", "Nucleo", "", "w_formas", Rarity.RARO, 2)
    )

    @Test
    fun `la mision entrega su cristal al completarla`() {
        assertEquals("cr1", CollectibleEngine.rewardFor(mision, starsObtained = 1, alreadyUnlocked = emptySet()))
    }

    @Test
    fun `sin estrellas no hay cristal`() {
        assertNull(CollectibleEngine.rewardFor(mision, starsObtained = 0, alreadyUnlocked = emptySet()))
    }

    @Test
    fun `un cristal no se entrega dos veces`() {
        assertNull(CollectibleEngine.rewardFor(mision, starsObtained = 3, alreadyUnlocked = setOf("cr1")))
    }

    @Test
    fun `una mision sin premio no entrega nada`() {
        val sinPremio = mision.copy(rewardCollectibleId = null)
        assertNull(CollectibleEngine.rewardFor(sinPremio, 3, emptySet()))
    }

    @Test
    fun `los hitos dependen de logros reales`() {
        val nada = CollectibleEngine.milestoneUnlocks(hitos, emptySet(), emptySet(), 1, 0, 0)
        assertTrue(nada.isEmpty())

        val conIsla = CollectibleEngine.milestoneUnlocks(
            hitos, emptySet(), setOf("w_formas"), level = 5, totalStars = 0, badgesUnlocked = 0
        )
        assertEquals(setOf("cr_hito_formas", "cr_hito_nivel5"), conIsla.map { it.id }.toSet())
    }

    @Test
    fun `porcentaje de la coleccion`() {
        assertEquals(50, CollectibleEngine.completionPercent(total = 10, unlocked = 5))
        assertEquals(0, CollectibleEngine.completionPercent(total = 0, unlocked = 3))
        assertEquals(100, CollectibleEngine.completionPercent(total = 4, unlocked = 9))
    }

    @Test
    fun `pista para conseguir un cristal`() {
        val cristal = Collectible("cr1", "Cristal", "", "w1", Rarity.COMUN, 1)
        assertTrue(CollectibleEngine.hintFor(cristal, listOf(mision)).contains("Mision"))
    }

    // --------------------------------------------------------------- repaso

    private fun reto(id: String) = Challenge(
        id = id, missionId = "m1", order = 1, kind = GameKind.QUIZ,
        prompt = "p", explanation = "e", hint = "h",
        payload = QuizPayload(listOf("a", "b"), 0)
    )

    private fun item(id: String, fallos: Int, cuando: Long, resuelto: Boolean = false) =
        ReviewItem(id, "m1", "w1", fallos, cuando, resuelto)

    @Test
    fun `la sesion prioriza los retos mas fallados`() {
        val items = listOf(
            item("c1", 1, 100),
            item("c2", 5, 200),
            item("c3", 3, 50)
        )
        val retos = listOf(reto("c1"), reto("c2"), reto("c3"))
        val sesion = ReviewEngine.buildSession(items, retos)
        assertEquals(listOf("c2", "c3", "c1"), sesion.map { it.id })
    }

    @Test
    fun `los retos resueltos no vuelven al repaso`() {
        val items = listOf(item("c1", 2, 100, resuelto = true), item("c2", 1, 200))
        val sesion = ReviewEngine.buildSession(items, listOf(reto("c1"), reto("c2")))
        assertEquals(listOf("c2"), sesion.map { it.id })
    }

    @Test
    fun `la sesion respeta el tamano maximo`() {
        val items = (1..10).map { item("c$it", it, it.toLong()) }
        val retos = (1..10).map { reto("c$it") }
        assertEquals(4, ReviewEngine.buildSession(items, retos, size = 4).size)
        assertTrue(ReviewEngine.buildSession(items, retos, size = 0).isEmpty())
    }

    @Test
    fun `un fallo nuevo entra en la lista y un acierto la resuelve`() {
        val vacia = emptyList<ReviewItem>()
        val conFallo = ReviewEngine.register(vacia, item("c1", 1, 10), correct = false)
        assertEquals(1, conFallo.size)
        assertEquals(1, conFallo.first().wrongCount)

        val segundoFallo = ReviewEngine.register(conFallo, item("c1", 1, 20), correct = false)
        assertEquals(2, segundoFallo.first().wrongCount)

        val resuelto = ReviewEngine.register(segundoFallo, item("c1", 1, 30), correct = true)
        assertTrue(resuelto.first().resolved)
    }

    @Test
    fun `acertar un reto que no estaba en la lista no la ensucia`() {
        val resultado = ReviewEngine.register(emptyList(), item("c1", 1, 10), correct = true)
        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `contador y mensaje de pendientes`() {
        val items = listOf(item("c1", 1, 10), item("c2", 1, 10, resuelto = true))
        assertEquals(1, ReviewEngine.pendingCount(items))
        assertTrue(ReviewEngine.statusMessage(0).contains("reluciente"))
        assertTrue(ReviewEngine.statusMessage(5).contains("5"))
    }

    @Test
    fun `el repaso solo se supera sin fallos`() {
        assertTrue(ReviewEngine.isSessionCleared(correct = 4, total = 4))
        assertFalse(ReviewEngine.isSessionCleared(correct = 3, total = 4))
        assertFalse(ReviewEngine.isSessionCleared(correct = 0, total = 0))
    }
}
