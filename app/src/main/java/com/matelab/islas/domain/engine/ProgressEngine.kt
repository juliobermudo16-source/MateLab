package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.domain.model.World

/**
 * Motor de progresion: estrellas, experiencia, niveles y desbloqueos.
 *
 * Todas las recompensas se derivan de acciones reales del nino; ninguna
 * cifra esta escrita a mano en la interfaz.
 */
object ProgressEngine {

    const val MAX_LEVEL = 30

    /** XP acumulado necesario para alcanzar [level]. Nivel 1 empieza en 0. */
    fun cumulativeXpFor(level: Int): Int {
        require(level >= 1) { "El nivel minimo es 1" }
        val steps = level - 1
        return 100 * steps + 25 * steps * (steps - 1)
    }

    fun levelFor(xp: Int): Int {
        if (xp <= 0) return 1
        var level = 1
        while (level < MAX_LEVEL && cumulativeXpFor(level + 1) <= xp) {
            level++
        }
        return level
    }

    /** XP que falta para el siguiente nivel (0 si ya esta al maximo). */
    fun xpToNextLevel(xp: Int): Int {
        val level = levelFor(xp)
        if (level >= MAX_LEVEL) return 0
        return (cumulativeXpFor(level + 1) - xp).coerceAtLeast(0)
    }

    /** Avance dentro del nivel actual, de 0f a 1f. */
    fun levelProgress(xp: Int): Float {
        val level = levelFor(xp)
        if (level >= MAX_LEVEL) return 1f
        val start = cumulativeXpFor(level)
        val end = cumulativeXpFor(level + 1)
        if (end <= start) return 1f
        return ((xp - start).toFloat() / (end - start).toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Estrellas de una mision.
     * 3 estrellas exigen pleno de aciertos y ninguna pista.
     */
    fun starsFor(correct: Int, total: Int, hintsUsed: Int): Int {
        if (total <= 0) return 0
        val safeCorrect = correct.coerceIn(0, total)
        val percent = safeCorrect * 100 / total
        val base = when {
            percent >= 100 -> 3
            percent >= 75 -> 2
            percent >= 50 -> 1
            else -> 0
        }
        return if (base == 3 && hintsUsed > 0) 2 else base
    }

    /** XP ganado en una mision. Depende de aciertos, estrellas y dificultad. */
    fun xpFor(correct: Int, stars: Int, difficulty: Difficulty): Int {
        val safeCorrect = correct.coerceAtLeast(0)
        return safeCorrect * 10 * difficulty.xpMultiplier + stars.coerceIn(0, 3) * 5
    }

    /** Una isla emerge cuando el jugador acumula el XP necesario. */
    fun isWorldUnlocked(world: World, xp: Int): Boolean = xp >= world.xpToUnlock

    /** Una mision se abre si su isla esta abierta y sus requisitos cumplidos. */
    fun isMissionUnlocked(
        mission: Mission,
        worldUnlocked: Boolean,
        completedMissionIds: Set<String>
    ): Boolean {
        if (!worldUnlocked) return false
        return mission.requires.all { it in completedMissionIds }
    }

    /** Estado visual definitivo de una mision. */
    fun statusFor(
        mission: Mission,
        progress: MissionProgress?,
        worldUnlocked: Boolean,
        completedMissionIds: Set<String>
    ): MissionStatus {
        if (!isMissionUnlocked(mission, worldUnlocked, completedMissionIds)) {
            return MissionStatus.BLOQUEADA
        }
        if (progress == null || progress.timesPlayed == 0) return MissionStatus.DISPONIBLE
        return when {
            progress.stars >= 3 -> MissionStatus.DOMINADA
            progress.stars >= 1 -> MissionStatus.COMPLETADA
            else -> MissionStatus.EMPEZADA
        }
    }

    /** Una mision cuenta como completada a partir de 1 estrella. */
    fun isCompleted(progress: MissionProgress?): Boolean = (progress?.stars ?: 0) >= 1

    /** Porcentaje de una isla, medido en estrellas conseguidas sobre el maximo. */
    fun worldPercent(missionIds: List<String>, progressById: Map<String, MissionProgress>): Int {
        if (missionIds.isEmpty()) return 0
        val got = missionIds.sumOf { progressById[it]?.stars ?: 0 }
        val max = missionIds.size * 3
        return (got * 100) / max
    }

    /** El mejor resultado nunca baja aunque el nino repita y le salga peor. */
    fun mergeProgress(previous: MissionProgress?, missionId: String, stars: Int, percent: Int, now: Long): MissionProgress {
        val before = previous ?: MissionProgress(missionId)
        return before.copy(
            missionId = missionId,
            stars = maxOf(before.stars, stars),
            bestPercent = maxOf(before.bestPercent, percent),
            timesPlayed = before.timesPlayed + 1,
            lastPlayedAt = now,
            status = when {
                maxOf(before.stars, stars) >= 3 -> MissionStatus.DOMINADA
                maxOf(before.stars, stars) >= 1 -> MissionStatus.COMPLETADA
                else -> MissionStatus.EMPEZADA
            }
        )
    }

    /** Racha diaria: sube si se juega al dia siguiente, se reinicia si se salta. */
    fun nextStreak(previousStreak: Int, lastPlayedDay: Long, today: Long): Int = when {
        lastPlayedDay == 0L -> 1
        today == lastPlayedDay -> previousStreak.coerceAtLeast(1)
        today == lastPlayedDay + 1 -> previousStreak + 1
        today > lastPlayedDay -> 1
        else -> previousStreak.coerceAtLeast(1)
    }
}
