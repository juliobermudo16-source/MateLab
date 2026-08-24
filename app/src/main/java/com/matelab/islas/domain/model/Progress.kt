package com.matelab.islas.domain.model

/**
 * Modelos de estado del jugador. Todo esto se persiste en Room.
 */

/** Perfil local. Nunca se pide nombre real ni ningun dato de contacto. */
data class Profile(
    val alias: String = "",
    val avatarId: Int = 0,
    val xp: Int = 0,
    val crystals: Int = 0,
    val streakDays: Int = 1,
    val lastPlayedDay: Long = 0L,
    val onboardingDone: Boolean = false,
    val profileDone: Boolean = false
) {
    val level: Int get() = com.matelab.islas.domain.engine.ProgressEngine.levelFor(xp)
}

/** Ajustes accesibles desde la cabina de Kubo. */
data class Settings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val bigTextEnabled: Boolean = false
)

/** Progreso persistido de una mision. */
data class MissionProgress(
    val missionId: String,
    val status: MissionStatus = MissionStatus.BLOQUEADA,
    val stars: Int = 0,
    val bestPercent: Int = 0,
    val timesPlayed: Int = 0,
    val lastPlayedAt: Long = 0L
)

/** Un intento concreto sobre un reto. Alimenta estadisticas y repaso. */
data class Attempt(
    val id: Long = 0L,
    val challengeId: String,
    val missionId: String,
    val worldId: String,
    val correct: Boolean,
    val usedHint: Boolean,
    val elapsedMs: Long,
    val timestamp: Long
)

/** Resumen calculado a partir de la tabla de intentos. */
data class Stats(
    val totalAttempts: Int = 0,
    val correctAttempts: Int = 0,
    val missionsCompleted: Int = 0,
    val totalStars: Int = 0,
    val badgesUnlocked: Int = 0,
    val crystalsUnlocked: Int = 0,
    val perWorldAccuracy: Map<String, Int> = emptyMap(),
    val perWorldAttempts: Map<String, Int> = emptyMap(),
    val last7Days: List<Int> = emptyList()
) {
    val accuracyPercent: Int
        get() = if (totalAttempts == 0) 0 else (correctAttempts * 100) / totalAttempts
}

/** Reto marcado para repasar porque se fallo. */
data class ReviewItem(
    val challengeId: String,
    val missionId: String,
    val worldId: String,
    val wrongCount: Int,
    val lastWrongAt: Long,
    val resolved: Boolean
)

/** Resultado de jugar una mision completa. */
data class MissionOutcome(
    val missionId: String,
    val correct: Int,
    val total: Int,
    val hintsUsed: Int,
    val elapsedMs: Long,
    val stars: Int,
    val xpEarned: Int,
    val newBadges: List<Badge> = emptyList(),
    val newCollectibles: List<Collectible> = emptyList(),
    val leveledUpTo: Int? = null,
    val unlockedWorlds: List<World> = emptyList()
) {
    val percent: Int get() = if (total == 0) 0 else (correct * 100) / total
}
