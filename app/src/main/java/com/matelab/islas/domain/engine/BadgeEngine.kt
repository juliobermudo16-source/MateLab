package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.BadgeRule

/**
 * Fotografia del progreso con la que se evaluan las insignias.
 * Todos los campos salen de la base de datos, ninguno es inventado.
 */
data class BadgeContext(
    val missionsCompleted: Int = 0,
    val perfectMissions: Int = 0,
    val noHintMissions: Int = 0,
    val totalStars: Int = 0,
    val xp: Int = 0,
    val streakDays: Int = 0,
    val collectionSize: Int = 0,
    val pendingReviewItems: Int = 0,
    val reviewSessionsCleared: Int = 0,
    /** worldId -> misiones completadas en esa isla. */
    val completedByWorld: Map<String, Int> = emptyMap(),
    /** worldId -> total de misiones de esa isla. */
    val totalByWorld: Map<String, Int> = emptyMap(),
    /** worldId -> porcentaje de aciertos en esa isla. */
    val accuracyByWorld: Map<String, Int> = emptyMap()
)

/**
 * Motor de insignias. Es puro: dado un contexto dice que insignias se cumplen.
 */
object BadgeEngine {

    fun isEarned(badge: Badge, ctx: BadgeContext): Boolean = when (badge.rule) {
        BadgeRule.FIRST_MISSION -> ctx.missionsCompleted >= 1
        BadgeRule.WORLD_COMPLETE -> {
            val worldId = badge.param
            val total = ctx.totalByWorld[worldId] ?: 0
            val done = ctx.completedByWorld[worldId] ?: 0
            total > 0 && done >= total
        }
        BadgeRule.PERFECT_MISSION -> ctx.perfectMissions >= badge.threshold
        BadgeRule.TOTAL_STARS -> ctx.totalStars >= badge.threshold
        BadgeRule.TOTAL_XP -> ctx.xp >= badge.threshold
        BadgeRule.STREAK_DAYS -> ctx.streakDays >= badge.threshold
        BadgeRule.COLLECTION_SIZE -> ctx.collectionSize >= badge.threshold
        BadgeRule.REVIEW_CLEARED -> ctx.reviewSessionsCleared >= badge.threshold
        BadgeRule.NO_HINT_MISSION -> ctx.noHintMissions >= badge.threshold
        BadgeRule.TOPIC_MASTER -> {
            val acc = ctx.accuracyByWorld[badge.param] ?: 0
            val done = ctx.completedByWorld[badge.param] ?: 0
            acc >= badge.threshold && done >= 3
        }
    }

    /** Insignias que se cumplen ahora y todavia no estaban desbloqueadas. */
    fun newlyEarned(
        all: List<Badge>,
        alreadyUnlocked: Set<String>,
        ctx: BadgeContext
    ): List<Badge> = all.filter { it.id !in alreadyUnlocked && isEarned(it, ctx) }

    /** Avance 0..1 de una insignia con umbral, para la pantalla de logros. */
    fun progressOf(badge: Badge, ctx: BadgeContext): Float {
        if (isEarned(badge, ctx)) return 1f
        val current = when (badge.rule) {
            BadgeRule.PERFECT_MISSION -> ctx.perfectMissions
            BadgeRule.TOTAL_STARS -> ctx.totalStars
            BadgeRule.TOTAL_XP -> ctx.xp
            BadgeRule.STREAK_DAYS -> ctx.streakDays
            BadgeRule.COLLECTION_SIZE -> ctx.collectionSize
            BadgeRule.REVIEW_CLEARED -> ctx.reviewSessionsCleared
            BadgeRule.NO_HINT_MISSION -> ctx.noHintMissions
            BadgeRule.FIRST_MISSION -> ctx.missionsCompleted
            BadgeRule.WORLD_COMPLETE -> ctx.completedByWorld[badge.param] ?: 0
            BadgeRule.TOPIC_MASTER -> ctx.accuracyByWorld[badge.param] ?: 0
        }
        val target = when (badge.rule) {
            BadgeRule.FIRST_MISSION -> 1
            BadgeRule.WORLD_COMPLETE -> (ctx.totalByWorld[badge.param] ?: 1).coerceAtLeast(1)
            else -> badge.threshold.coerceAtLeast(1)
        }
        return (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    }

    /** Texto tipo "3 / 10" para la tarjeta de la insignia. */
    fun progressLabel(badge: Badge, ctx: BadgeContext): String {
        val pct = (progressOf(badge, ctx) * 100).toInt()
        return "$pct %"
    }
}
