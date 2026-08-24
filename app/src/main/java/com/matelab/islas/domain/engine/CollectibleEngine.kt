package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission

/**
 * Reglas de desbloqueo de los Cristales de Ingenio.
 *
 * Un cristal solo aparece si el nino ha hecho algo real:
 * completar una mision, terminar una isla o alcanzar un nivel.
 */
object CollectibleEngine {

    /** Cristales de hito, no ligados a una mision concreta. */
    const val MILESTONE_PREFIX = "cr_hito_"

    /**
     * Cristal que entrega la mision al completarse por primera vez.
     * Devuelve null si la mision no premia cristal o ya estaba desbloqueado.
     */
    fun rewardFor(
        mission: Mission,
        starsObtained: Int,
        alreadyUnlocked: Set<String>
    ): String? {
        val reward = mission.rewardCollectibleId ?: return null
        if (starsObtained < 1) return null
        if (reward in alreadyUnlocked) return null
        return reward
    }

    /**
     * Cristales de hito que se cumplen ahora.
     * [worldsCompleted] son las islas terminadas al 100 %.
     */
    fun milestoneUnlocks(
        all: List<Collectible>,
        alreadyUnlocked: Set<String>,
        worldsCompleted: Set<String>,
        level: Int,
        totalStars: Int,
        badgesUnlocked: Int
    ): List<Collectible> {
        val earned = mutableListOf<Collectible>()
        for (c in all.filter { it.id.startsWith(MILESTONE_PREFIX) && it.id !in alreadyUnlocked }) {
            val ok = when (c.id) {
                "cr_hito_formas" -> "w_formas" in worldsCompleted
                "cr_hito_medida" -> "w_medida" in worldsCompleted
                "cr_hito_fraccion" -> "w_fraccion" in worldsCompleted
                "cr_hito_numeros" -> "w_numeros" in worldsCompleted
                "cr_hito_nivel5" -> level >= 5
                "cr_hito_nivel10" -> level >= 10
                "cr_hito_estrellas" -> totalStars >= 30
                "cr_hito_insignias" -> badgesUnlocked >= 8
                else -> false
            }
            if (ok) earned += c
        }
        return earned
    }

    /** Porcentaje de la coleccion completada. */
    fun completionPercent(total: Int, unlocked: Int): Int {
        if (total <= 0) return 0
        return (unlocked.coerceAtMost(total) * 100) / total
    }

    /** Pista de como conseguir un cristal aun bloqueado. */
    fun hintFor(collectible: Collectible, missions: List<Mission>): String {
        if (collectible.id.startsWith(MILESTONE_PREFIX)) {
            return when (collectible.id) {
                "cr_hito_formas" -> "Termina todas las misiones de la Bahia de las Formas."
                "cr_hito_medida" -> "Termina todas las misiones de Puerto Medida."
                "cr_hito_fraccion" -> "Termina todas las misiones del Volcan Fraccion."
                "cr_hito_numeros" -> "Termina todas las misiones de la Cueva de los Numeros."
                "cr_hito_nivel5" -> "Alcanza el nivel 5."
                "cr_hito_nivel10" -> "Alcanza el nivel 10."
                "cr_hito_estrellas" -> "Consigue 30 estrellas en total."
                "cr_hito_insignias" -> "Desbloquea 8 insignias."
                else -> "Sigue explorando el archipielago."
            }
        }
        val mission = missions.firstOrNull { it.rewardCollectibleId == collectible.id }
        return if (mission != null) {
            "Completa la mision \"${mission.name}\"."
        } else {
            "Sigue explorando el archipielago."
        }
    }
}
